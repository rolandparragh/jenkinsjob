package com.example.ferm.repository;

import com.example.ferm.domain.NotificationAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationAlertRepository extends JpaRepository<NotificationAlert, UUID> {

    List<NotificationAlert> findByAcknowledgedAtIsNullOrderByCreatedAtDesc();
}
