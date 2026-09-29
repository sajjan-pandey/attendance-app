package com.attendance.entity;

import java.time.LocalDateTime;

public record AttendanceRecord(String userCode, String userName, String locationCode,
                               AttendanceType attendanceType, double latitude, double longitude,
                               double locationLatitude, double locationLongitude,
                               double distanceMeters, double allowedRadiusMeters,
                               LocalDateTime recordedAt) { }
