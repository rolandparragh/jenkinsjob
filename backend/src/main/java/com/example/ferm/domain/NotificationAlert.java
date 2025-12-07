package com.example.ferm.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_alerts")
public class NotificationAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ferm_id", nullable = false)
    private Long fermId;

    @Column(name = "ferm_name", nullable = false)
    private String fermName;

    @Column(name = "timer_label", nullable = false)
    private String timerLabel;

    @Column(nullable = false)
    private String message;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    protected NotificationAlert() {
    }

    public NotificationAlert(Long fermId, String fermName, String timerLabel, String message, String severity) {
        this.fermId = fermId;
        this.fermName = fermName;
        this.timerLabel = timerLabel;
        this.message = message;
        this.severity = severity;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Long getFermId() {
        return fermId;
    }

    public String getFermName() {
        return fermName;
    }

    public String getTimerLabel() {
        return timerLabel;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public boolean isAcknowledged() {
        return acknowledgedAt != null;
    }

    public void acknowledge() {
        this.acknowledgedAt = Instant.now();
    }
}
