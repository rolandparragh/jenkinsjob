package com.example.ferm.dto;

import com.example.ferm.service.TimerColor;

import java.time.Instant;

public record TimerDto(
        String label,
        int durationHours,
        long durationSeconds,
        long remainingSeconds,
        long secondsPastZero,
        TimerColor color,
        boolean running,
        boolean stopped,
        boolean breachActive,
        Instant startedAt,
        Instant targetAt,
        Instant stoppedAt
) {
}
