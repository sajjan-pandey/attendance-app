package com.attendance.entity;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID tenantId; private UUID projectId; private UUID userId; private UUID locationId;
    @Enumerated(EnumType.STRING) private AttendanceType attendanceType;
    private OffsetDateTime punchedAt;
    private double punchLatitude; private double punchLongitude;
    private double locationLatitude; private double locationLongitude;
    private double calculatedDistanceMeters; private double allowedRadiusMeters;
    private UUID faceVerificationId; private String decision; private String rejectionReason;
    @Column(nullable = false) private String idempotencyKey;
    protected AttendanceRecordEntity() { }

    public AttendanceRecordEntity(AttendanceRecord record) {
        this.attendanceType = record.attendanceType();
        this.punchedAt = record.recordedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        this.punchLatitude = record.latitude();
        this.punchLongitude = record.longitude();
        this.locationLatitude = record.locationLatitude();
        this.locationLongitude = record.locationLongitude();
        this.calculatedDistanceMeters = record.distanceMeters();
        this.allowedRadiusMeters = record.allowedRadiusMeters();
        this.decision = "ALLOWED";
        this.idempotencyKey = record.userCode() + "-" + record.recordedAt().toLocalDate() + "-" + record.attendanceType();
    }

    public UUID getId() { return id; }
}
