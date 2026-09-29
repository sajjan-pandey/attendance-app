package com.attendance.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.AttendanceResultResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.service.AttendanceDecision;

@Component
public class AttendanceMapper {
    public AttendanceResponse toResponse(AttendanceRecord record) {
        if (record == null) return null;
        return new AttendanceResponse(record.userCode(), record.userName(), record.locationCode(),
                record.attendanceType(), record.latitude(), record.longitude(), record.locationLatitude(),
                record.locationLongitude(), record.distanceMeters(), record.allowedRadiusMeters(), record.recordedAt());
    }
    public AttendanceResultResponse toResponse(AttendanceDecision decision) {
        return new AttendanceResultResponse(decision.success(), decision.message(), toResponse(decision.record()));
    }
    public List<AttendanceResponse> toResponseList(List<AttendanceRecord> records) {
        return records.stream().map(this::toResponse).toList();
    }
}
