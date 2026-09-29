package com.attendance.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.AttendanceType;

@Repository
public class InMemoryAttendanceRepository implements AttendanceRepository {
    private final Map<String, AttendanceRecord> records = new ConcurrentHashMap<>();
    public void save(AttendanceRecord record) { records.put(record.userCode() + "-" + record.recordedAt().toLocalDate() + "-" + record.attendanceType(), record); }
    public List<AttendanceRecord> findAll() { return records.values().stream().toList(); }
    public boolean existsForToday(String userCode, AttendanceType type) {
        return records.values().stream().anyMatch(record -> record.userCode().equals(userCode)
                && record.attendanceType() == type && record.recordedAt().toLocalDate().equals(java.time.LocalDate.now()));
    }
}
