package com.example.ferm.service;

import com.example.ferm.config.TimerProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TimerCalculator {

    private final TimerProperties properties;

    public TimerCalculator(TimerProperties properties) {
        this.properties = properties;
    }

    public TimerStatus statusFor(Instant startedAt, Instant stoppedAt, Duration duration) {
        long durationSeconds = duration.getSeconds();

        if (startedAt == null) {
            return new TimerStatus(false, false, false, durationSeconds, durationSeconds, 0, TimerColor.GRAY, false, null, null, null);
        }

        Instant targetAt = startedAt.plus(duration);
        Instant now = Instant.now();
        boolean stopped = stoppedAt != null;
        Instant reference = stopped ? stoppedAt : now;

        long deltaSeconds = Duration.between(reference, targetAt).getSeconds();
        long secondsRemaining = Math.max(0, deltaSeconds);
        long secondsPastZero = deltaSeconds < 0 ? Math.abs(deltaSeconds) : 0;
        boolean running = !stopped && reference.isBefore(targetAt);
        boolean breachActive = secondsPastZero >= properties.breachDuration().getSeconds();

        TimerColor color = resolveColor(startedAt, stopped, secondsRemaining, secondsPastZero);

        return new TimerStatus(
                true,
                running,
                stopped,
                durationSeconds,
                secondsRemaining,
                secondsPastZero,
                color,
                breachActive,
                startedAt,
                targetAt,
                stoppedAt
        );
    }

    private TimerColor resolveColor(Instant startedAt, boolean stopped, long secondsRemaining, long secondsPastZero) {
        if (startedAt == null || stopped) {
            return TimerColor.GRAY;
        }
        if (secondsPastZero >= properties.breachDuration().getSeconds()) {
            return TimerColor.RED;
        }
        if (secondsRemaining <= properties.warningDuration().getSeconds()) {
            return TimerColor.YELLOW;
        }
        return TimerColor.GREEN;
    }
}
