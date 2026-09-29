package com.attendance.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "camp_schedules")
public class CampScheduleEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID tenantId; private UUID projectId; private UUID locationId;
    private LocalDate scheduleDate; private LocalTime startTime; private LocalTime endTime;
    private String status = "ACTIVE";
    protected CampScheduleEntity() { }
}
