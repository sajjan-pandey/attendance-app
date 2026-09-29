package com.attendance.entity;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "attendance_policies")
public class AttendancePolicyEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID tenantId;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 30) private String policyType;
    @Column(nullable = false) private double defaultRadiusMeters = 50;
    protected AttendancePolicyEntity() { }
}
