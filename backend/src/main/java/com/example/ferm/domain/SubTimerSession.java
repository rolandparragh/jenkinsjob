package com.example.ferm.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sub_timer_sessions")
public class SubTimerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private TimerSession session;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completion_logged", nullable = false)
    private boolean completionLogged = false;

    @Column(name = "breach_logged", nullable = false)
    private boolean breachLogged = false;

    protected SubTimerSession() {
    }

    public SubTimerSession(TimerSession session, int durationHours, Instant startedAt) {
        this.session = session;
        this.durationHours = durationHours;
        this.startedAt = startedAt;
    }

    public UUID getId() {
        return id;
    }

    public TimerSession getSession() {
        return session;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getStoppedAt() {
        return stoppedAt;
    }

    public void stop(Instant stoppedAt) {
        this.stoppedAt = stoppedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isCompletionLogged() {
        return completionLogged;
    }

    public void setCompletionLogged(boolean completionLogged) {
        this.completionLogged = completionLogged;
    }

    public boolean isBreachLogged() {
        return breachLogged;
    }

    public void setBreachLogged(boolean breachLogged) {
        this.breachLogged = breachLogged;
    }
}
