package com.attendance.dto.request;

public class FaceEnrollmentRequest {
    private String userCode;
    private String faceImageBase64;
    private String sourceImageBase64;
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getFaceImageBase64() { return faceImageBase64; }
    public void setFaceImageBase64(String faceImageBase64) { this.faceImageBase64 = faceImageBase64; }
    public String getSourceImageBase64() { return sourceImageBase64; }
    public void setSourceImageBase64(String sourceImageBase64) { this.sourceImageBase64 = sourceImageBase64; }
}
