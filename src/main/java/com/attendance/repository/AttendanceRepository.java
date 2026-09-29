package com.attendance.repository;

import java.util.List;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.AttendanceType;

public interface AttendanceRepository {
    void save(AttendanceRecord record);
    List<AttendanceRecord> findAll();
    boolean existsForToday(String userCode, AttendanceType type);
}
