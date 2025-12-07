package com.example.ferm.repository;

import com.example.ferm.domain.SubTimerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubTimerSessionRepository extends JpaRepository<SubTimerSession, UUID> {

    List<SubTimerSession> findBySessionId(UUID sessionId);
}
