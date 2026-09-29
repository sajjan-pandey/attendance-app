package com.attendance.entity;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class LocationEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID tenantId;
    @Column(nullable = false) private UUID projectId;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 80) private String code;
    @Column(nullable = false, length = 30) private String type;
    // Hibernate 7 does not allow precision/scale on Java double (SQL floating point).
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(nullable = false) private double radiusMeters = 50;
    protected LocationEntity() { }
}
