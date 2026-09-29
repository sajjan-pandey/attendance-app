package com.attendance.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "liveness_sessions")
public class LivenessSessionEntity {
    @Id
    @Column(length = 36)
    private String sessionId;
    @Column(nullable = false, length = 80)
    private String userCode;
    @Column(nullable = false, columnDefinition = "text")
    private String sequence;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant completedAt;
    @Column(name = "nonce", nullable = false, length = 36,
            columnDefinition = "varchar(36) default gen_random_uuid()::text")
    private String nonce;
    @Column(name = "stage", nullable = false, columnDefinition = "integer default 0") private int stage;
    @Column(name = "frame_count", nullable = false, columnDefinition = "bigint default 0") private long frameCount;
    @Column(name = "last_sequence", nullable = false, columnDefinition = "bigint default 0") private long lastSequence;
    @Column(name = "baseline_x", nullable = false, columnDefinition = "double precision default 0") private double baselineX;
    @Column(name = "baseline_y", nullable = false, columnDefinition = "double precision default 0") private double baselineY;
    @Column(name = "blink_baseline", nullable = false, columnDefinition = "double precision default 0") private double blinkBaseline;
    @Column(name = "blink_open_seen", nullable = false, columnDefinition = "boolean default false") private boolean blinkOpenSeen;
    @Column(name = "blink_closed", nullable = false, columnDefinition = "boolean default false") private boolean blinkClosed;
    @Column(name = "server_passed", nullable = false, columnDefinition = "boolean default false") private boolean serverPassed;
    // Store client IP to prevent session replay from different locations
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    protected LivenessSessionEntity() { }

    public LivenessSessionEntity(String sessionId, String userCode, String sequence, Instant createdAt) {
        this(sessionId, userCode, sequence, java.util.UUID.randomUUID().toString(), createdAt);
    }
    public LivenessSessionEntity(String sessionId, String userCode, String sequence, String nonce, Instant createdAt) {
        this.sessionId = sessionId;
        this.userCode = userCode;
        this.sequence = sequence;
        this.nonce = nonce;
        this.createdAt = createdAt;
    }

    public String getSessionId() { return sessionId; }
    public String getUserCode() { return userCode; }
    public String getSequence() { return sequence; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getNonce() { return nonce; }
    public int getStage() { return stage; }
    public long getFrameCount() { return frameCount; }
    public long getLastSequence() { return lastSequence; }
    public double getBaselineX() { return baselineX; }
    public double getBaselineY() { return baselineY; }
    public double getBlinkBaseline() { return blinkBaseline; }
    public boolean isBlinkOpenSeen() { return blinkOpenSeen; }
    public boolean isBlinkClosed() { return blinkClosed; }
    public boolean isServerPassed() { return serverPassed; }
    public void updateFrame(long sequence, int stage, double x, double y, double blinkBaseline,
                            boolean openSeen, boolean closed, boolean passed) {
        this.lastSequence = sequence; this.frameCount++; this.stage = stage;
        this.baselineX = x; this.baselineY = y; this.blinkBaseline = blinkBaseline;
        this.blinkOpenSeen = openSeen; this.blinkClosed = closed; this.serverPassed = passed;
    }
    public String getClientIp() { return clientIp; }
    public void markCompleted(Instant completedAt) { this.completedAt = completedAt; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
}
