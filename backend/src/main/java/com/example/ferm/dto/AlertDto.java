package com.example.ferm.dto;

import java.time.Instant;
import java.util.UUID;

public record AlertDto(
        UUID id,
        Long fermId,
        String fermName,
        String timerLabel,
        String severity,
        String message,
        Instant createdAt
) {
}
