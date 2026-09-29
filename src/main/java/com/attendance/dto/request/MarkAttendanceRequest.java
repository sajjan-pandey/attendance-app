package com.attendance.dto.request;

public class MarkAttendanceRequest {
    private String userCode;
    private String locationCode;
    private double latitude;
    private double longitude;
    private String attendanceType;
    private String faceImageBase64;
    private String sourceImageBase64;
    private String livenessSessionId;

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAttendanceType() { return attendanceType; }
    public void setAttendanceType(String attendanceType) { this.attendanceType = attendanceType; }
    public String getFaceImageBase64() { return faceImageBase64; }
    public void setFaceImageBase64(String faceImageBase64) { this.faceImageBase64 = faceImageBase64; }
    public String getSourceImageBase64() { return sourceImageBase64; }
    public void setSourceImageBase64(String sourceImageBase64) { this.sourceImageBase64 = sourceImageBase64; }
    public String getLivenessSessionId() { return livenessSessionId; }
    public void setLivenessSessionId(String livenessSessionId) { this.livenessSessionId = livenessSessionId; }
}
