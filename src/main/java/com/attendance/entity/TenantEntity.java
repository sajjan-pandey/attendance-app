package com.attendance.entity;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "tenants")
public class TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(columnDefinition = "text") private String description;
    @Column(nullable = false, length = 20) private String status = "ACTIVE";
    protected TenantEntity() { }
}
