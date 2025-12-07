package com.example.ferm.dto;

import java.util.List;

public record FermDto(
        Long id,
        String name,
        TimerDto mainTimer,
        List<TimerDto> subTimers,
        boolean running
) {
}
