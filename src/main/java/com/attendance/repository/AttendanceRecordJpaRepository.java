package com.attendance.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.attendance.entity.AttendanceRecordEntity;

public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecordEntity, UUID> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
