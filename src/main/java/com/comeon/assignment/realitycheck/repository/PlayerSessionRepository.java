package com.comeon.assignment.realitycheck.repository;

import com.comeon.assignment.realitycheck.entity.PlayerSession;
import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerSessionRepository extends JpaRepository<PlayerSession, Long> {

    Optional<PlayerSession> findByPlayerIdAndStatus(Long playerId, PlayerSessionStatus status);

    List<PlayerSession> findByStatus(PlayerSessionStatus status);
}
