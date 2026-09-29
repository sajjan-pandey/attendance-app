package com.attendance.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import com.attendance.service.LivenessChallengeService;

@RestController
@RequestMapping("/api/v1/liveness")
public class LivenessController {
    private static final Logger log = LoggerFactory.getLogger(LivenessController.class);
    private final LivenessChallengeService service;

    public LivenessController(LivenessChallengeService service) { this.service = service; }

    @PostMapping("/start")
    public LivenessChallengeService.Challenge start(@RequestParam String userCode, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        return service.start(userCode, clientIp);
    }

    @PostMapping("/complete")
    public CompletionResponse complete(@RequestBody CompletionRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        try {
            service.complete(request.sessionId(), request.userCode(), request.completedSequence(), request.clientDurationMillis(), clientIp);
        } catch (IllegalArgumentException exception) {
            log.warn("Liveness completion rejected: sessionId={}, userCode={}, reason={}",
                    request.sessionId(), request.userCode(), exception.getMessage());
            throw exception;
        }
        return new CompletionResponse(true, "Liveness proof accepted.");
    }

    @PostMapping("/frame")
    public LivenessChallengeService.FrameResult frame(@RequestBody LivenessChallengeService.FrameRequest request) {
        LivenessChallengeService.FrameResult result;
        try {
            result = service.processFrame(request);
        } catch (IllegalArgumentException exception) {
            log.warn("Liveness frame rejected: sessionId={}, userCode={}, sequenceNumber={}, reason={}",
                    request.sessionId(), request.userCode(), request.sequenceNumber(), exception.getMessage());
            throw exception;
        }
        log.info("Liveness frame processed: sessionId={}, userCode={}, sequenceNumber={}, accepted={}, passed={}, stage={}/{}",
                request.sessionId(), request.userCode(), request.sequenceNumber(), result.accepted(),
                result.passed(), result.stage(), result.totalStages());
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For (take first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CompletionResponse invalid(IllegalArgumentException exception) {
        log.warn("Liveness request rejected: reason={}", exception.getMessage());
        return new CompletionResponse(false, exception.getMessage());
    }

    public record CompletionRequest(String sessionId, String userCode, List<String> completedSequence,
                                    long clientDurationMillis) { }
    public record CompletionResponse(boolean verified, String message) { }
}
