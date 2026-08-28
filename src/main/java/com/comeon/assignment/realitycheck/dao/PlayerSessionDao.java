package com.comeon.assignment.realitycheck.dao;

import com.comeon.assignment.realitycheck.entity.PlayerSession;
import com.comeon.assignment.realitycheck.repository.PlayerSessionRepository;
import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus.ACTIVE;

@Service
@RequiredArgsConstructor
public class PlayerSessionDao {

    private final PlayerSessionRepository playerSessionRepository;

    @CachePut(
            cacheNames = "playerSessions",
            key = "{#playerId, #playerSessionStatus}"
    )
    public PlayerSession getActiveSession(long playerId, PlayerSessionStatus playerSessionStatus) {
        return playerSessionRepository.findByPlayerIdAndStatus(playerId, playerSessionStatus)
                .orElseThrow(() -> new NoSuchElementException("Player is not active or player session not found"));
    }

    public Optional<PlayerSession> getActiveOptionalSession(long playerId) {
        return playerSessionRepository.findByPlayerIdAndStatus(playerId, ACTIVE);
    }

    @CachePut(
            cacheNames = "playerSessions",
            key = "{#playerId, #playerSession.status}"
    )
    public PlayerSession savePlayerSession(PlayerSession playerSession, long playerId) {
        return playerSessionRepository.save(playerSession);
    }

    public List<PlayerSession> getAllActivePlayerSessions() {
        return playerSessionRepository.findByStatus(ACTIVE);
    }

}
