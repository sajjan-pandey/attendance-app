package com.attendance.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "face_enrollments")
public class FaceEnrollmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true, length = 80) private String userCode;
    // A face descriptor is JSON text, not a PostgreSQL large object. Using
    // @Lob makes the PostgreSQL driver read it as an OID/Clob and fails when
    // verification runs outside an explicit transaction.
    @Column(nullable = false, columnDefinition = "text") private String embeddingJson;
    @Column(nullable = false) private String modelVersion;
    @Column(nullable = false) private OffsetDateTime consentedAt;
    private String status = "ACTIVE";
    protected FaceEnrollmentEntity() { }
    public FaceEnrollmentEntity(String userCode, String embeddingJson, String modelVersion) {
        this.userCode = userCode; this.embeddingJson = embeddingJson; this.modelVersion = modelVersion;
        this.consentedAt = OffsetDateTime.now();
    }
    public String getUserCode() { return userCode; }
    public String getEmbeddingJson() { return embeddingJson; }
    public String getModelVersion() { return modelVersion; }
    public void updateEmbedding(String embeddingJson) { this.embeddingJson = embeddingJson; this.consentedAt = OffsetDateTime.now(); }
    public void updateEmbedding(String embeddingJson, String modelVersion) {
        this.embeddingJson = embeddingJson; this.modelVersion = modelVersion; this.consentedAt = OffsetDateTime.now();
    }
}
