package com.example.ferm.service;

import java.time.Instant;

public record TimerStatus(
        boolean started,
        boolean running,
        boolean stopped,
        long durationSeconds,
        long secondsRemaining,
        long secondsPastZero,
        TimerColor color,
        boolean breachActive,
        Instant startedAt,
        Instant targetAt,
        Instant stoppedAt) {
}
