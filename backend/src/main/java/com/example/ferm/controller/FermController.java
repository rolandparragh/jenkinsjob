package com.example.ferm.controller;

import com.example.ferm.domain.FermSegment;
import com.example.ferm.domain.TimerSession;
import com.example.ferm.dto.FermDto;
import com.example.ferm.service.TimerService;
import com.example.ferm.service.TimerViewMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ferms")
public class FermController {

    private final TimerService timerService;
    private final TimerViewMapper mapper;

    public FermController(TimerService timerService, TimerViewMapper mapper) {
        this.timerService = timerService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<FermDto> list() {
        Map<Long, TimerSession> active = timerService.activeSessionsByFerm();
        return timerService.allFerms().stream()
                .map(ferm -> mapper.toDto(ferm.getId(), ferm.getName(), active.get(ferm.getId())))
                .toList();
    }

    @PostMapping("/{id}/start")
    @ResponseStatus(HttpStatus.CREATED)
    public FermDto start(@PathVariable Long id) {
        TimerSession session = timerService.start(id);
        return mapper.toDto(session.getFerm().getId(), session.getFerm().getName(), session);
    }

    @PostMapping("/{id}/sub-timers/{duration}/stop")
    public FermDto stopSubTimer(@PathVariable Long id, @PathVariable int duration) {
        timerService.stopSubTimer(id, duration);
        Map<Long, TimerSession> active = timerService.activeSessionsByFerm();
        TimerSession session = active.get(id);
        String name = session != null ? session.getFerm().getName() : "Ferm " + id;
        return mapper.toDto(id, name, session);
    }

    @PostMapping("/{id}/reset")
    public FermDto resetAllTimers(@PathVariable Long id) {
        timerService.resetAllTimers(id);
        // Get ferm name from repository to avoid lazy loading issues
        FermSegment ferm = timerService.allFerms().stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Ferm segment not found"));
        // After reset, session is no longer active, so pass null to show idle state
        return mapper.toDto(id, ferm.getName(), null);
    }
}
