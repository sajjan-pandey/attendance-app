package com.attendance.dto.response;

public record AttendanceResultResponse(boolean success, String message, AttendanceResponse attendance) { }
