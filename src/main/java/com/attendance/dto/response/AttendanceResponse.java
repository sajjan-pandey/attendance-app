package com.attendance.dto.response;

import java.time.LocalDateTime;
import com.attendance.entity.AttendanceType;

public record AttendanceResponse(String userCode, String userName, String locationCode,
                                 AttendanceType attendanceType, double latitude, double longitude,
                                 double locationLatitude, double locationLongitude,
                                 double distanceMeters, double allowedRadiusMeters,
                                 LocalDateTime recordedAt) { }
