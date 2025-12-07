package com.example.ferm.service;

import com.example.ferm.config.TimerProperties;
import com.example.ferm.domain.SubTimerSession;
import com.example.ferm.domain.TimerSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class TimerMonitor {

    private final TimerService timerService;
    private final TimerProperties properties;
    private final AlertService alertService;

    public TimerMonitor(TimerService timerService, TimerProperties properties, AlertService alertService) {
        this.timerService = timerService;
        this.properties = properties;
        this.alertService = alertService;
    }

    @Scheduled(fixedDelayString = "60000")
    @Transactional
    public void evaluateTimers() {
        List<TimerSession> sessions = timerService.sessionsPendingAudit();
        Instant now = Instant.now();
        for (TimerSession session : sessions) {
            monitorMain(session, now);
            session.getSubTimers().forEach(sub -> monitorSub(session, sub, now));
        }
    }

    private void monitorMain(TimerSession session, Instant now) {
        Instant completesAt = session.getStartedAt().plus(properties.mainDuration());
        if (session.getCompletedAt() == null && !now.isBefore(completesAt)) {
            timerService.markMainCompletion(session, completesAt);
        }

        Instant breachAt = completesAt.plus(properties.breachDuration());
        if (!session.isBreachLogged() && !now.isBefore(breachAt)) {
            timerService.markMainBreach(session);
            alertService.createAlert(
                    session.getFerm().getId(),
                    session.getFerm().getName(),
                    "Main 50h",
                    "Ferm %d main timer breached 15 min threshold.".formatted(session.getFerm().getId()),
                    "CRITICAL"
            );
        }
    }

    private void monitorSub(TimerSession session, SubTimerSession sub, Instant now) {
        if (sub.getStoppedAt() != null) {
            return;
        }

        Duration duration = Duration.ofHours(sub.getDurationHours());
        Instant completesAt = sub.getStartedAt().plus(duration);
        if (sub.getCompletedAt() == null && !now.isBefore(completesAt)) {
            timerService.markSubCompletion(sub, session, completesAt);
        }

        Instant breachAt = completesAt.plus(properties.breachDuration());
        if (!sub.isBreachLogged() && !now.isBefore(breachAt)) {
            timerService.markSubBreach(sub, session);
            alertService.createAlert(
                    session.getFerm().getId(),
                    session.getFerm().getName(),
                    sub.getDurationHours() + "h",
                    "Ferm %d timer %dh breached.".formatted(session.getFerm().getId(), sub.getDurationHours()),
                    "WARNING"
            );
        }
    }
}
