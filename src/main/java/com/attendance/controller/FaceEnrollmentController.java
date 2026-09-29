package com.attendance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.attendance.dto.request.FaceEnrollmentRequest;
import com.attendance.dto.response.FaceVerificationResponse;
import com.attendance.service.FaceRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/face")
public class FaceEnrollmentController {
    private static final Logger log = LoggerFactory.getLogger(FaceEnrollmentController.class);
    private final FaceRecognitionService faceRecognitionService;

    public FaceEnrollmentController(FaceRecognitionService faceRecognitionService) {
        this.faceRecognitionService = faceRecognitionService;
    }

    @PostMapping("/enroll")
    public FaceVerificationResponse enroll(@RequestBody FaceEnrollmentRequest request) {
        if (request.getFaceImageBase64() == null || request.getFaceImageBase64().isBlank())
            throw new IllegalArgumentException("Face image is required for ArcFace enrollment.");
        faceRecognitionService.enrollImage(request.getUserCode(), request.getFaceImageBase64(), request.getSourceImageBase64());
        return new FaceVerificationResponse(true, "Face enrolled successfully.", 0);
    }

    @GetMapping("/status")
    public FaceVerificationResponse enrollmentStatus(@RequestParam String userCode) {
        boolean enrolled = faceRecognitionService.hasCompatibleEnrollment(userCode);
        return new FaceVerificationResponse(enrolled,
                enrolled ? "Face enrollment found." : "Face enrollment required.", 0);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FaceVerificationResponse invalidFace(IllegalArgumentException exception) {
        log.warn("Face enrollment request rejected: reason={}", exception.getMessage());
        return new FaceVerificationResponse(false, exception.getMessage(), -1);
    }
}
