package com.attendance.service;

import com.attendance.entity.DemoUser;

public interface FaceVerificationService {
    boolean verifyImage(DemoUser user, String faceImageBase64);
    boolean verifyImage(DemoUser user, String faceImageBase64, String sourceImageBase64);
}
