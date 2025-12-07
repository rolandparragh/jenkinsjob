package com.example.ferm.service;

import com.example.ferm.config.TimerProperties;
import com.example.ferm.domain.SubTimerSession;
import com.example.ferm.domain.TimerSession;
import com.example.ferm.dto.FermDto;
import com.example.ferm.dto.TimerDto;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimerViewMapper {

    private final TimerCalculator calculator;
    private final TimerProperties properties;

    public TimerViewMapper(TimerCalculator calculator, TimerProperties properties) {
        this.calculator = calculator;
        this.properties = properties;
    }

    public FermDto toDto(Long fermId, String name, TimerSession session) {
        TimerDto main = mapMainTimer(session);
        List<TimerDto> subTimers = mapSubTimers(session);
        boolean running = session != null;
        return new FermDto(fermId, name, main, subTimers, running);
    }

    private TimerDto mapMainTimer(TimerSession session) {
        Duration duration = properties.mainDuration();
        TimerStatus status = session == null
                ? calculator.statusFor(null, null, duration)
                : calculator.statusFor(session.getStartedAt(), session.getStoppedAt(), duration);

        int hours = Math.toIntExact(duration.toHours());
        return new TimerDto(
                "Main " + hours + "h",
                hours,
                status.durationSeconds(),
                status.secondsRemaining(),
                status.secondsPastZero(),
                status.color(),
                status.running(),
                status.stopped(),
                status.breachActive(),
                status.startedAt(),
                status.targetAt(),
                status.stoppedAt()
        );
    }

    private List<TimerDto> mapSubTimers(TimerSession session) {
        if (session == null) {
            return properties.getSubTimers().stream()
                    .sorted()
                    .map(hours -> idleSubTimer(hours))
                    .toList();
        }

        return session.getSubTimers().stream()
                .sorted(Comparator.comparingInt(SubTimerSession::getDurationHours))
                .map(this::activeSubTimer)
                .collect(Collectors.toList());
    }

    private TimerDto idleSubTimer(int hours) {
        TimerStatus status = calculator.statusFor(null, null, Duration.ofHours(hours));
        return new TimerDto(
                hours + "h",
                hours,
                status.durationSeconds(),
                status.secondsRemaining(),
                status.secondsPastZero(),
                status.color(),
                false,
                false,
                false,
                null,
                null,
                null
        );
    }

    private TimerDto activeSubTimer(SubTimerSession sub) {
        Duration duration = Duration.ofHours(sub.getDurationHours());
        TimerStatus status = calculator.statusFor(sub.getStartedAt(), sub.getStoppedAt(), duration);
        return new TimerDto(
                sub.getDurationHours() + "h",
                sub.getDurationHours(),
                status.durationSeconds(),
                status.secondsRemaining(),
                status.secondsPastZero(),
                status.color(),
                status.running(),
                status.stopped(),
                status.breachActive(),
                status.startedAt(),
                status.targetAt(),
                status.stoppedAt()
        );
    }
}
