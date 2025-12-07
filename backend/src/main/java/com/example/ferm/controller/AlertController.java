package com.example.ferm.controller;

import com.example.ferm.dto.AlertDto;
import com.example.ferm.service.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertDto> listActive() {
        return alertService.findActiveAlerts().stream()
                .map(alert -> new AlertDto(
                        alert.getId(),
                        alert.getFermId(),
                        alert.getFermName(),
                        alert.getTimerLabel(),
                        alert.getSeverity(),
                        alert.getMessage(),
                        alert.getCreatedAt()))
                .toList();
    }

    @PostMapping("/{id}/ack")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acknowledge(@PathVariable UUID id) {
        alertService.acknowledge(id);
    }
}
