package com.attendance.repository;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import com.attendance.entity.LivenessSessionEntity;

public interface LivenessSessionRepository extends JpaRepository<LivenessSessionEntity, String> {
    long deleteByCreatedAtBefore(Instant cutoff);
}
