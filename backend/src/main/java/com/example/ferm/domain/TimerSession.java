package com.example.ferm.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "timer_sessions")
public class TimerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ferm_id")
    private FermSegment ferm;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completion_logged", nullable = false)
    private boolean completionLogged = false;

    @Column(name = "log_file", nullable = false)
    private String logFile;

    @Column(name = "breach_logged", nullable = false)
    private boolean breachLogged = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SubTimerSession> subTimers = new HashSet<>();

    protected TimerSession() {
    }

    public TimerSession(FermSegment ferm, Instant startedAt, String logFile) {
        this.ferm = ferm;
        this.startedAt = startedAt;
        this.logFile = logFile;
    }

    public UUID getId() {
        return id;
    }

    public FermSegment getFerm() {
        return ferm;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getStoppedAt() {
        return stoppedAt;
    }

    public void stop(Instant stoppedAt) {
        this.stoppedAt = stoppedAt;
        this.active = false;
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

    public String getLogFile() {
        return logFile;
    }

    public boolean isBreachLogged() {
        return breachLogged;
    }

    public void setBreachLogged(boolean breachLogged) {
        this.breachLogged = breachLogged;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<SubTimerSession> getSubTimers() {
        return subTimers;
    }
}
