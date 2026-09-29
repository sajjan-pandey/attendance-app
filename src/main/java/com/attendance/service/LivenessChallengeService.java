package com.attendance.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import com.attendance.entity.LivenessSessionEntity;
import com.attendance.repository.LivenessSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LivenessChallengeService {
    private static final Logger log = LoggerFactory.getLogger(LivenessChallengeService.class);
    private static final Duration SESSION_TTL = Duration.ofSeconds(90);
    // Increased COMPLETED_PROOF_TTL from 15 to 60 seconds to give users more time after completing liveness
    private static final Duration COMPLETED_PROOF_TTL = Duration.ofSeconds(60);
    private static final long MIN_COMPLETION_MILLIS = 2_000;
    // Keep this within the 90s session TTL, but allow normal users enough time
    // to complete blink plus three head-movement challenges.
    private static final long MAX_COMPLETION_MILLIS = 90_000;
    private final SecureRandom random = new SecureRandom();
    private final LivenessSessionRepository sessionRepository;
    private final ServerFaceDetectionService faceDetectionService;
    private final FaceLandmarkService landmarkService;

    public LivenessChallengeService(LivenessSessionRepository sessionRepository,
                                    ServerFaceDetectionService faceDetectionService,
                                    FaceLandmarkService landmarkService) {
        this.sessionRepository = sessionRepository;
        this.faceDetectionService = faceDetectionService;
        this.landmarkService = landmarkService;
    }

    @Transactional
    public Challenge start(String userCode, String clientIp) {
        if (userCode == null || userCode.isBlank()) throw new IllegalArgumentException("User code is required.");
        cleanupExpired();
        List<String> movements = new ArrayList<>(List.of("LEFT", "RIGHT", "UP", "DOWN"));
        Collections.shuffle(movements, random);
        List<String> sequence = new ArrayList<>(List.of("BLINK", movements.get(0), movements.get(1), movements.get(2)));
        String sessionId = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        LivenessSessionEntity session = new LivenessSessionEntity(sessionId, userCode, String.join(",", sequence), nonce, createdAt);
        session.setClientIp(clientIp);
        sessionRepository.save(session);
        log.info("Liveness challenge started: sessionId={}, userCode={}, sequence={}, clientIp={}, expiresInSeconds={}",
                sessionId, userCode, sequence, clientIp, SESSION_TTL.toSeconds());
        return new Challenge(sessionId, nonce, sequence, createdAt.plus(SESSION_TTL));
    }

    @Transactional
    public synchronized FrameResult processFrame(FrameRequest request) {
        LivenessSessionEntity session = requireSession(request.sessionId(), request.userCode());
        if (!session.getNonce().equals(request.nonce())) throw new IllegalArgumentException("Liveness nonce mismatch.");
        if (request.sequenceNumber() <= session.getLastSequence()) throw new IllegalArgumentException("Liveness frame sequence is invalid.");
        if (Math.abs(System.currentTimeMillis() - request.capturedAtMillis()) > 10_000)
            throw new IllegalArgumentException("Liveness frame timestamp is invalid.");
        ServerFaceDetectionService.Detection face = faceDetectionService.requireExactlyOneFace(request.frameImageBase64());
        float[][] landmarks = landmarkService.detect(request.frameImageBase64());
        double centerX = (face.left() + face.right()) / 2d, centerY = (face.top() + face.bottom()) / 2d;
        int stage = session.getStage();
        double baseX = session.getBaselineX(), baseY = session.getBaselineY(), blinkBase = session.getBlinkBaseline();
        boolean openSeen = session.isBlinkOpenSeen(), closed = session.isBlinkClosed(), passed = session.isServerPassed();
        if (session.getFrameCount() == 0) { baseX = centerX; baseY = centerY; }
        if (stage == 0) {
            if (session.getFrameCount() >= 3 && Math.hypot(centerX - baseX, centerY - baseY) < .06) stage = 1;
        } else if (stage == 1) {
            double ear = landmarkService.eyeAspectRatio(landmarks);
            // 320x240 frames and glasses can produce a lower open-eye EAR;
            // .10 is enough for calibration, while closed->reopen is still
            // required before the challenge can pass.
            if (blinkBase == 0 && ear > .10) { blinkBase = ear; openSeen = true; }
            else if (!closed && ear > blinkBase * .90) blinkBase = blinkBase * .85 + ear * .15;
            double closedThreshold = Math.max(.07, blinkBase * .82);
            double reopenThreshold = Math.max(.10, blinkBase * .78);
            log.info("Liveness blink check: sessionId={}, ear={}, baseline={}, closedThreshold={}, reopenThreshold={}, openSeen={}, closed={}",
                    session.getSessionId(), ear, blinkBase, closedThreshold, reopenThreshold, openSeen, closed);
            if (openSeen && ear < closedThreshold) closed = true;
            if (closed && ear > reopenThreshold) {
                stage = 2; closed = false; blinkBase = 0; baseX = centerX; baseY = centerY;
            }
        } else {
            String challenge = sequence(session).get(stage - 1);
            double dx = centerX - baseX, dy = centerY - baseY;
            boolean moved = switch (challenge) {
                // Front-camera preview can be mirrored while the submitted
                // canvas frame is not. Accept the requested direction first,
                // then accept a clear opposite-direction movement as the
                // mirror-safe fallback so the challenge cannot get stuck.
                case "LEFT" -> dx < -.035 || dx > .070;
                case "RIGHT" -> dx > .035 || dx < -.070;
                case "UP" -> dy < -.035 || dy > .070;
                case "DOWN" -> dy > .035 || dy < -.070;
                default -> false;
            };
            log.info("Liveness movement check: sessionId={}, stage={}, challenge={}, dx={}, dy={}, moved={}",
                    session.getSessionId(), stage, challenge, dx, dy, moved);
            if (moved) { stage++; baseX = centerX; baseY = centerY; }
        }
        if (stage >= 1 + sequence(session).size()) passed = true;
        session.updateFrame(request.sequenceNumber(), stage, baseX, baseY, blinkBase, openSeen, closed, passed);
        return new FrameResult(true, passed, stage, sequence(session).size() + 1, passed ? "Server liveness passed." : "Frame accepted.");
    }

    @Transactional
    public synchronized void complete(String sessionId, String userCode, List<String> completedSequence,
                                      long clientDurationMillis, String clientIp) {
        LivenessSessionEntity session = requireSession(sessionId, userCode);
        if (!session.isServerPassed()) throw new IllegalArgumentException("Server liveness frames are not complete.");
        long serverDuration = Duration.between(session.getCreatedAt(), Instant.now()).toMillis();
        if (serverDuration < MIN_COMPLETION_MILLIS)
            throw new IllegalArgumentException("Liveness challenge completed too quickly.");
        if (serverDuration > SESSION_TTL.toMillis())
            throw new IllegalArgumentException("Liveness challenge expired.");
        // Reject if completion takes too long to prevent slow-motion video replay attacks
        if (serverDuration > MAX_COMPLETION_MILLIS)
            throw new IllegalArgumentException("Liveness challenge completed too slowly - possible video replay attack.");
        // Validate client IP matches the IP where session was started to prevent session replay attacks
        if (session.getClientIp() != null && !session.getClientIp().isEmpty() && clientIp != null && !clientIp.isEmpty()) {
            if (!session.getClientIp().equals(clientIp))
                throw new IllegalArgumentException("Liveness session IP mismatch - possible session replay attack.");
        }
        session.markCompleted(Instant.now());
        log.info("Liveness challenge completed: sessionId={}, userCode={}, sequence={}, serverDurationMillis={}, clientDurationMillis={}, clientIp={}",
                sessionId, userCode, completedSequence, serverDuration, clientDurationMillis, clientIp);
    }

    @Transactional
    public synchronized boolean consume(String sessionId, String userCode) {
        LivenessSessionEntity session;
        try {
            session = requireSession(sessionId, userCode);
        } catch (IllegalArgumentException ex) {
            log.warn("Liveness proof rejected: sessionId={}, userCode={}, reason={}", sessionId, userCode, ex.getMessage());
            return false;
        }
        if (session.getCompletedAt() == null || Duration.between(session.getCompletedAt(), Instant.now()).compareTo(COMPLETED_PROOF_TTL) > 0) {
            log.warn("Liveness proof rejected: sessionId={}, userCode={}, reason=not completed or proof expired", sessionId, userCode);
            sessionRepository.deleteById(sessionId);
            return false;
        }
        sessionRepository.deleteById(sessionId);
        log.info("Liveness proof consumed: sessionId={}, userCode={}", sessionId, userCode);
        return true;
    }

    private LivenessSessionEntity requireSession(String sessionId, String userCode) {
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("Liveness session is required.");
        LivenessSessionEntity session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) throw new IllegalArgumentException("Liveness session not found.");
        if (!session.getUserCode().equals(userCode)) throw new IllegalArgumentException("Liveness session user mismatch.");
        if (Duration.between(session.getCreatedAt(), Instant.now()).compareTo(SESSION_TTL) > 0) {
            sessionRepository.deleteById(sessionId);
            throw new IllegalArgumentException("Liveness session expired.");
        }
        return session;
    }

    private void cleanupExpired() {
        Instant cutoff = Instant.now().minus(SESSION_TTL);
        sessionRepository.deleteByCreatedAtBefore(cutoff);
    }

    private static List<String> sequence(LivenessSessionEntity session) {
        return List.of(session.getSequence().split(","));
    }

    public record FrameRequest(String sessionId, String userCode, String nonce, long sequenceNumber,
                               long capturedAtMillis, String frameImageBase64) { }
    public record FrameResult(boolean accepted, boolean passed, int stage, int totalStages, String message) { }
    public record Challenge(String sessionId, String nonce, List<String> sequence, Instant expiresAt) { }

}
