package com.attendance.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "user_location_assignments")
public class UserLocationAssignmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID tenantId; private UUID projectId; private UUID userId; private UUID locationId;
    private LocalDate assignmentDate; private LocalTime startTime; private LocalTime endTime;
    private String status = "ACTIVE";
    protected UserLocationAssignmentEntity() { }
}
