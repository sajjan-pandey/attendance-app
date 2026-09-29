package com.attendance.dto.response;

public record FaceVerificationResponse(boolean verified, String message, double distance) { }
