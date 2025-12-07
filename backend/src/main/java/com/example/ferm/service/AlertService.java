package com.example.ferm.service;

import com.example.ferm.domain.NotificationAlert;
import com.example.ferm.repository.NotificationAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private final NotificationAlertRepository repository;

    public AlertService(NotificationAlertRepository repository) {
        this.repository = repository;
    }

    public NotificationAlert createAlert(Long fermId, String fermName, String timerLabel, String message, String severity) {
        NotificationAlert alert = new NotificationAlert(fermId, fermName, timerLabel, message, severity);
        return repository.save(alert);
    }

    public List<NotificationAlert> findActiveAlerts() {
        return repository.findByAcknowledgedAtIsNullOrderByCreatedAtDesc();
    }

    @Transactional
    public void acknowledge(UUID alertId) {
        repository.findById(alertId).ifPresent(alert -> {
            if (!alert.isAcknowledged()) {
                alert.acknowledge();
            }
        });
    }
}
