package com.attendance.service;

import com.attendance.entity.AttendanceRecord;

public record AttendanceDecision(boolean success, String message, AttendanceRecord record) {
    public static AttendanceDecision accepted(AttendanceRecord record) { return new AttendanceDecision(true, "Attendance marked successfully.", record); }
    public static AttendanceDecision rejected(String message) { return new AttendanceDecision(false, message, null); }
}
