package com.attendance.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.attendance.entity.FaceEnrollmentEntity;

public interface FaceEnrollmentRepository extends JpaRepository<FaceEnrollmentEntity, UUID> {
    Optional<FaceEnrollmentEntity> findByUserCodeAndStatus(String userCode, String status);
    List<FaceEnrollmentEntity> findByStatus(String status);
}
