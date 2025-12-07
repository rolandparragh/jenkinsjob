package com.example.ferm.repository;

import com.example.ferm.domain.TimerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimerSessionRepository extends JpaRepository<TimerSession, UUID> {

    Optional<TimerSession> findFirstByFermIdAndActiveTrue(Long fermId);

    List<TimerSession> findByActiveTrue();

    List<TimerSession> findByBreachLoggedFalse();
}
