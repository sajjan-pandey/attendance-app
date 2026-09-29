package com.attendance.service;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.attendance.entity.DemoUser;
import com.attendance.entity.FaceEnrollmentEntity;
import com.attendance.repository.FaceEnrollmentRepository;

@Service
public class FaceRecognitionService implements FaceVerificationService {
    private static final Logger log = LoggerFactory.getLogger(FaceRecognitionService.class);
    private static final int EMBEDDING_DIMENSIONS = 512;
    // face-api Euclidean distance and ArcFace cosine distance are different
    // metrics. This threshold is calibrated for the ArcFace ONNX pipeline.
    private static final String ARCFACE_MODEL_VERSION = "arcface-w600k-mbf-align-v3";
    private final double arcfaceDistanceThreshold;
    private final double arcfaceMinBestMatchMargin;
    private final FaceEnrollmentRepository enrollmentRepository;
    private final OnnxArcFaceService onnxArcFaceService;
    private final FaceImageQualityService faceImageQualityService;
    private final ServerFaceDetectionService serverFaceDetectionService;
    
    public FaceRecognitionService(FaceEnrollmentRepository enrollmentRepository, OnnxArcFaceService onnxArcFaceService,
                                  FaceImageQualityService faceImageQualityService,
                                  ServerFaceDetectionService serverFaceDetectionService,
                                  @Value("${face.recognition.distance-threshold:0.60}") double arcfaceDistanceThreshold,
                                  @Value("${face.recognition.min-best-match-margin:0.05}") double arcfaceMinBestMatchMargin) {
        this.enrollmentRepository = enrollmentRepository;
        this.onnxArcFaceService = onnxArcFaceService;
        this.faceImageQualityService = faceImageQualityService;
        this.serverFaceDetectionService = serverFaceDetectionService;
        if (arcfaceDistanceThreshold <= 0 || arcfaceDistanceThreshold >= 2 || arcfaceMinBestMatchMargin < 0)
            throw new IllegalArgumentException("Invalid ArcFace threshold configuration.");
        this.arcfaceDistanceThreshold = arcfaceDistanceThreshold;
        this.arcfaceMinBestMatchMargin = arcfaceMinBestMatchMargin;
    }

    // All enrollment uses the same ArcFace image pipeline as attendance
    // verification, so legacy browser-descriptor enrollment is removed.
    public void enrollImage(String userCode, String imageBase64) {
        enrollImage(userCode, imageBase64, null);
    }

    public void enrollImage(String userCode, String imageBase64, String sourceImageBase64) {
        log.info("Face enrollment requested: userCode={}, source=arcface-image", userCode);
        faceImageQualityService.validateOrThrow(userCode, imageBase64);
        serverFaceDetectionService.requireExactlyOneFace(sourceImageBase64 == null || sourceImageBase64.isBlank()
                ? imageBase64 : sourceImageBase64);
        enrollEmbedding(userCode, onnxArcFaceService.embeddingFromBase64(imageBase64), ARCFACE_MODEL_VERSION);
    }

    private void enrollEmbedding(String userCode, String embeddingJson, String modelVersion) {
        double threshold = arcfaceDistanceThreshold;
        for (FaceEnrollmentEntity existing : enrollmentRepository.findByStatus("ACTIVE")) {
            try {
                if (!modelVersion.equals(existing.getModelVersion())) {
                    log.info("Skipping enrollment from older preprocessing version: requestedUserCode={}, existingUserCode={}, existingVersion={}, requiredVersion={}",
                            userCode, existing.getUserCode(), existing.getModelVersion(), modelVersion);
                    continue;
                }
                if (!existing.getUserCode().equals(userCode)) {
                    double duplicateDistance = embeddingDistance(existing.getEmbeddingJson(), embeddingJson);
                    log.info("Face enrollment duplicate check: requestedUserCode={}, existingUserCode={}, distance={}, threshold={}",
                            userCode, existing.getUserCode(), duplicateDistance, threshold);
                    if (duplicateDistance <= threshold) {
                        log.warn("Face enrollment rejected: requestedUserCode={}, reason=face already enrolled, existingUserCode={}, distance={}",
                                userCode, existing.getUserCode(), duplicateDistance);
                        throw new IllegalArgumentException("This face is already enrolled for user " + existing.getUserCode() + ".");
                    }
                }
            } catch (IllegalArgumentException ex) {
                if (ex.getMessage() != null && ex.getMessage().startsWith("This face is already enrolled for user ")) throw ex;
                log.warn("Skipping incompatible enrollment during duplicate check: requestedUserCode={}, existingUserCode={}, reason={}",
                        userCode, existing.getUserCode(), ex.getMessage());
            }
        }
        FaceEnrollmentEntity enrollment = enrollmentRepository.findByUserCodeAndStatus(userCode, "ACTIVE")
                .orElseGet(() -> new FaceEnrollmentEntity(userCode, embeddingJson, modelVersion));
        enrollment.updateEmbedding(embeddingJson, modelVersion);
        enrollmentRepository.save(enrollment);
        log.info("Face enrollment saved: userCode={}, modelVersion={}", userCode, modelVersion);
    }

    @Transactional(readOnly = true)
    public boolean hasCompatibleEnrollment(String userCode) {
        boolean compatible = enrollmentRepository.findByUserCodeAndStatus(userCode, "ACTIVE")
                .map(enrollment -> {
                    log.debug("Face enrollment status: userCode={}, status=ACTIVE, modelVersion={}, dimensions={}",
                            userCode, enrollment.getModelVersion(), safeDimensions(enrollment.getEmbeddingJson()));
                    try {
                        return ARCFACE_MODEL_VERSION.equals(enrollment.getModelVersion())
                                && parse(enrollment.getEmbeddingJson()).length == EMBEDDING_DIMENSIONS;
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                })
                .orElse(false);
        if (!compatible) log.warn("Face enrollment unavailable or incompatible: userCode={}", userCode);
        return compatible;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyImage(DemoUser user, String faceImageBase64) {
        return verifyImage(user, faceImageBase64, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyImage(DemoUser user, String faceImageBase64, String sourceImageBase64) {
        if (faceImageBase64 == null || faceImageBase64.isBlank()) {
            log.warn("ArcFace verification rejected: empty image, userCode={}", user.code());
            return false;
        }
        try {
            faceImageQualityService.validateOrThrow(user.code(), faceImageBase64);
            String sourceImage = sourceImageBase64 == null || sourceImageBase64.isBlank()
                    ? faceImageBase64 : sourceImageBase64;
            serverFaceDetectionService.requireExactlyOneFace(sourceImage);
        } catch (IllegalArgumentException ex) {
            log.warn("ArcFace verification rejected by backend image quality: userCode={}, reason={}", user.code(), ex.getMessage());
            return false;
        }
        return enrollmentRepository.findByUserCodeAndStatus(user.code(), "ACTIVE")
                .map(saved -> {
                    try {
                        log.info("ArcFace verification started: userCode={}, enrolledModel={}, enrolledDimensions={}",
                                user.code(), saved.getModelVersion(), safeDimensions(saved.getEmbeddingJson()));
                        if (!ARCFACE_MODEL_VERSION.equals(saved.getModelVersion())) {
                            log.warn("ArcFace verification rejected: incompatible preprocessing version, userCode={}, enrolledModel={}, requiredModel={}",
                                    user.code(), saved.getModelVersion(), ARCFACE_MODEL_VERSION);
                            return false;
                        }
                        String probe = onnxArcFaceService.embeddingFromBase64(faceImageBase64);
                        double distance = embeddingDistance(saved.getEmbeddingJson(), probe);
                        String bestUserCode = null;
                        double bestDistance = Double.POSITIVE_INFINITY;
                        double secondBestDistance = Double.POSITIVE_INFINITY;
                        for (FaceEnrollmentEntity candidate : enrollmentRepository.findByStatus("ACTIVE")) {
                            if (!ARCFACE_MODEL_VERSION.equals(candidate.getModelVersion())) continue;
                            try {
                                double candidateDistance = embeddingDistance(candidate.getEmbeddingJson(), probe);
                                if (candidateDistance < bestDistance) {
                                    secondBestDistance = bestDistance;
                                    bestDistance = candidateDistance;
                                    bestUserCode = candidate.getUserCode();
                                } else if (candidateDistance < secondBestDistance) {
                                    secondBestDistance = candidateDistance;
                                }
                            } catch (IllegalArgumentException ex) {
                                log.warn("Skipping incompatible ArcFace candidate: candidateUserCode={}, reason={}",
                                        candidate.getUserCode(), ex.getMessage());
                            }
                        }
                        boolean requestedUserIsBest = user.code().equals(bestUserCode);
                        boolean clearWinner = Double.isInfinite(secondBestDistance)
                                || secondBestDistance - bestDistance >= arcfaceMinBestMatchMargin;
                        boolean verified = Double.isFinite(distance)
                                && distance >= 0 && distance <= 2
                                && distance <= arcfaceDistanceThreshold
                                && requestedUserIsBest && clearWinner;
                        log.info("ArcFace verification result: userCode={}, distance={}, threshold={}, bestUserCode={}, bestDistance={}, secondBestDistance={}, minMargin={}, verified={}",
                                user.code(), distance, arcfaceDistanceThreshold, bestUserCode, bestDistance,
                                secondBestDistance, arcfaceMinBestMatchMargin, verified);
                        return verified;
                    } catch (RuntimeException ex) {
                        log.warn("ArcFace verification failed, userCode={}, reason={}", user.code(), ex.getMessage());
                        return false;
                    }
                }).orElseGet(() -> {
                    log.warn("ArcFace verification rejected: no active enrollment, userCode={}", user.code());
                    return false;
                });
    }


    private static int safeDimensions(String json) {
        try { return parse(json).length; }
        catch (IllegalArgumentException ex) { return -1; }
    }

    private static double embeddingDistance(String firstJson, String secondJson) {
        double[] first = parse(firstJson), second = parse(secondJson);
        if (first.length != EMBEDDING_DIMENSIONS || second.length != EMBEDDING_DIMENSIONS)
            throw new IllegalArgumentException("Embedding must contain exactly " + EMBEDDING_DIMENSIONS + " values");
        double dot = 0, firstNorm = 0, secondNorm = 0;
        for (int i = 0; i < first.length; i++) {
            if (!Double.isFinite(first[i]) || !Double.isFinite(second[i]))
                throw new IllegalArgumentException("Embedding contains a non-finite value");
            dot += first[i] * second[i]; firstNorm += first[i] * first[i]; secondNorm += second[i] * second[i];
        }
        double denominator = Math.sqrt(firstNorm) * Math.sqrt(secondNorm);
        if (!Double.isFinite(denominator) || denominator <= 1e-12)
            throw new IllegalArgumentException("Embedding has zero norm");
        double cosine = dot / denominator;
        cosine = Math.max(-1d, Math.min(1d, cosine));
        return 1d - cosine;
    }

    private static double[] parse(String json) {
        try {
            if (json == null || json.isBlank()) throw new IllegalArgumentException("Embedding is empty");
            String normalized=json.trim();
            if (!normalized.startsWith("[") || !normalized.endsWith("]"))
                throw new IllegalArgumentException("Embedding JSON array is invalid");
            double[] values=Arrays.stream(normalized.substring(1, normalized.length()-1).split(","))
                    .map(String::trim).filter(value -> !value.isEmpty())
                    .mapToDouble(Double::parseDouble).toArray();
            if (values.length != EMBEDDING_DIMENSIONS)
                throw new IllegalArgumentException("Embedding dimensions="+values.length+", expected="+EMBEDDING_DIMENSIONS);
            return values;
        }
        catch (Exception ex) { throw new IllegalArgumentException("Invalid face descriptor", ex); }
    }
}
