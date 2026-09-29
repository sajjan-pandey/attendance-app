package com.attendance.entity;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class ProjectEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID tenantId;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 50) private String code;
    @Column(length = 30) private String status = "ACTIVE";
    private UUID attendancePolicyId;
    protected ProjectEntity() { }
}
