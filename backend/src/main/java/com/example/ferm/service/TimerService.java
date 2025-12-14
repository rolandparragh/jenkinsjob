package com.example.ferm.service;

import com.example.ferm.config.TimerProperties;
import com.example.ferm.domain.FermSegment;
import com.example.ferm.domain.SubTimerSession;
import com.example.ferm.domain.TimerSession;
import com.example.ferm.repository.FermSegmentRepository;
import com.example.ferm.repository.TimerSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TimerService {

    private final FermSegmentRepository fermRepository;
    private final TimerSessionRepository sessionRepository;
    private final TimerProperties properties;
    private final LogbookService logbookService;

    public TimerService(FermSegmentRepository fermRepository,
                        TimerSessionRepository sessionRepository,
                        TimerProperties properties,
                        LogbookService logbookService) {
        this.fermRepository = fermRepository;
        this.sessionRepository = sessionRepository;
        this.properties = properties;
        this.logbookService = logbookService;
    }

    @Transactional
    public TimerSession start(Long fermId) {
        FermSegment ferm = fermRepository.findById(fermId)
                .orElseThrow(() -> new EntityNotFoundException("Ferm segment not found"));

        sessionRepository.findFirstByFermIdAndActiveTrue(fermId).ifPresent(existing -> {
            if (existing.getCompletedAt() == null) {
                throw new IllegalStateException("Ferm " + fermId + " already running");
            }
            existing.setActive(false);
            sessionRepository.save(existing);
        });

        Instant now = Instant.now();
        long mainHours = properties.mainDuration().toHours();
        String logFile = logbookService.createOrGetLogFile(fermId);
        logbookService.append(logFile, "Start button pressed. " + mainHours + "h timer initiated.");

        TimerSession session = new TimerSession(ferm, now, logFile);
        properties.getSubTimers().forEach(hours -> {
            SubTimerSession sub = new SubTimerSession(session, hours, now);
            session.getSubTimers().add(sub);
            logbookService.append(logFile, "Sub timer " + hours + "h started.");
        });

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public Map<Long, TimerSession> activeSessionsByFerm() {
        return sessionRepository.findByActiveTrue().stream()
                .collect(Collectors.toMap(session -> session.getFerm().getId(), session -> session));
    }

    @Transactional(readOnly = true)
    public List<TimerSession> activeSessions() {
        return sessionRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<TimerSession> sessionsPendingAudit() {
        return sessionRepository.findByBreachLoggedFalse();
    }

    @Transactional(readOnly = true)
    public List<FermSegment> allFerms() {
        return fermRepository.findAll().stream()
                .sorted(Comparator.comparingLong(FermSegment::getId))
                .toList();
    }

    @Transactional
    public SubTimerSession stopSubTimer(Long fermId, int durationHours) {
        TimerSession session = sessionRepository.findFirstByFermIdAndActiveTrue(fermId)
                .orElseThrow(() -> new IllegalStateException("No running timer for Ferm " + fermId));

        SubTimerSession sub = session.getSubTimers().stream()
                .filter(t -> t.getDurationHours() == durationHours)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Timer " + durationHours + "h not found."));

        if (sub.getStoppedAt() != null) {
            return sub;
        }

        Instant now = Instant.now();
        sub.stop(now);
        Duration elapsed = Duration.between(sub.getStartedAt(), now);
        logbookService.append(session.getLogFile(), "Sub timer " + durationHours + "h stopped at " + formatDuration(elapsed));
        return sub;
    }

    @Transactional
    public TimerSession resetAllTimers(Long fermId) {
        TimerSession session = sessionRepository.findFirstByFermIdAndActiveTrue(fermId)
                .orElseThrow(() -> new IllegalStateException("No running timer for Ferm " + fermId));

        Instant now = Instant.now();
        
        // Stop all subtimers that haven't been stopped yet
        session.getSubTimers().stream()
                .filter(sub -> sub.getStoppedAt() == null)
                .forEach(sub -> {
                    sub.stop(now);
                    Duration elapsed = Duration.between(sub.getStartedAt(), now);
                    logbookService.append(session.getLogFile(), "Sub timer " + sub.getDurationHours() + "h stopped at " + formatDuration(elapsed));
                });

        // Stop the main timer session
        session.stop(now);
        Duration mainElapsed = Duration.between(session.getStartedAt(), now);
        logbookService.append(session.getLogFile(), "All timers reset. Main timer stopped at " + formatDuration(mainElapsed));
        
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public TimerSession requireActiveSession(UUID sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow();
    }

    @Transactional
    public void markMainCompletion(TimerSession session, Instant completionAt) {
        if (session.getCompletedAt() == null) {
            session.setCompletedAt(completionAt);
            session.setCompletionLogged(true);
            logbookService.append(session.getLogFile(), "Main " + properties.mainDuration().toHours() + "h timer completed.");
        }
    }

    @Transactional
    public void markMainBreach(TimerSession session) {
        if (!session.isBreachLogged()) {
            session.setBreachLogged(true);
            logbookService.append(session.getLogFile(), "Main timer exceeded by 15 minutes. Breach recorded.");
        }
    }

    @Transactional
    public void markSubCompletion(SubTimerSession subTimer, TimerSession parent, Instant completedAt) {
        if (!subTimer.isCompletionLogged()) {
            subTimer.setCompletedAt(completedAt);
            subTimer.setCompletionLogged(true);
            logbookService.append(parent.getLogFile(), "Sub timer " + subTimer.getDurationHours() + "h completed.");
        }
    }

    @Transactional
    public void markSubBreach(SubTimerSession subTimer, TimerSession parent) {
        if (!subTimer.isBreachLogged()) {
            subTimer.setBreachLogged(true);
            logbookService.append(parent.getLogFile(), "Sub timer " + subTimer.getDurationHours() + "h exceeded by 15 minutes.");
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return hours + "h " + minutes + "m elapsed.";
    }
}
