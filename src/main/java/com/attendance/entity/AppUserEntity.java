package com.attendance.entity;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUserEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID tenantId;
    @Column(nullable = false) private UUID projectId;
    @Column(nullable = false, length = 80) private String userCode;
    @Column(nullable = false, length = 150) private String fullName;
    @Column(nullable = false, length = 20) private String status = "ACTIVE";
    protected AppUserEntity() { }
}
