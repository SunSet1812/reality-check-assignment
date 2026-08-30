package com.comeon.assignment.realitycheck.repository;

import com.comeon.assignment.realitycheck.entity.PlayerRecord;
import com.comeon.assignment.realitycheck.entity.PlayerSession;
import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PlayerSessionRepositoryTest {

    @Autowired
    private PlayerSessionRepository playerSessionRepository;

    @Autowired
    private PlayerRecordRepository playerRecordRepository;

    private PlayerSession playerSession;
    private PlayerRecord playerRecord;

    @BeforeEach
    void setUp() {

        playerRecord = PlayerRecord.builder()
                .franchiseId(10L)
                .timezone("Europe/Stockholm")
                .build();

        playerRecordRepository.save(playerRecord);

        playerSession = PlayerSession.builder()
                .player(playerRecord)
                .franchiseId(1L)
                .status(PlayerSessionStatus.ACTIVE)
                .intervalMinutes(30)
                .startedAt(Instant.now())
                .lastPromptAt(Instant.now())
                .elapsedSeconds(0L)
                .netAmountMinor(0L)
                .acknowledged(false)
                .nextCheckAt(Instant.now())
                .build();
    }

    @Test
    void save_shouldPersistPlayerSession() {

        PlayerSession savedPlayerSession = playerSessionRepository.save(playerSession);
        assertNotNull(savedPlayerSession); assertNotNull(savedPlayerSession.getId());
        Optional<PlayerSession> result = playerSessionRepository.findById(savedPlayerSession.getId());
        PlayerSession responsePlayerSession = null;
        if(result.isPresent())
            responsePlayerSession = result.get();
        assertNotNull(responsePlayerSession);
        assertEquals(playerSession.getPlayer().getId(), responsePlayerSession.getPlayer().getId());
        assertEquals(PlayerSessionStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    void findByPlayerIdAndStatus_shouldReturnPlayerSession() {

        playerSessionRepository.save(playerSession);
        Optional<PlayerSession> result =
                playerSessionRepository.findByPlayerIdAndStatus(
                        playerRecord.getId(),
                        PlayerSessionStatus.ACTIVE
                );
        assertTrue(result.isPresent());

        PlayerSession resultSession = result.get();

        assertEquals(
                playerRecord.getId(),
                resultSession.getPlayer().getId()
        );

        assertEquals(
                PlayerSessionStatus.ACTIVE,
                resultSession.getStatus()
        );
    }

    @Test
    void findByPlayerIdAndStatus_shouldReturnEmptyWhenNoMatchingStatusExists() {

        playerSessionRepository.save(playerSession);

        Optional<PlayerSession> result =
                playerSessionRepository.findByPlayerIdAndStatus(
                        playerRecord.getId(),
                        PlayerSessionStatus.NOT_ACTIVE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByPlayerIdAndStatus_shouldReturnEmptyWhenPlayerDoesNotExist() {

        playerSessionRepository.save(playerSession);

        Optional<PlayerSession> result =
                playerSessionRepository.findByPlayerIdAndStatus(
                        9999L,
                        PlayerSessionStatus.ACTIVE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByStatus_shouldReturnAllSessionsWithGivenStatus() {

        playerSessionRepository.save(playerSession);

        PlayerRecord secondPlayer = PlayerRecord.builder()
                .franchiseId(10L)
                .timezone("Europe/Stockholm")
                .build();
        playerRecordRepository.save(secondPlayer);

        PlayerSession secondPlayerSession = PlayerSession.builder()
                .player(secondPlayer)
                .franchiseId(1L)
                .status(PlayerSessionStatus.ACTIVE)
                .intervalMinutes(30)
                .startedAt(Instant.now())
                .lastPromptAt(Instant.now())
                .elapsedSeconds(0L)
                .netAmountMinor(0L)
                .acknowledged(false)
                .nextCheckAt(Instant.now())
                .build();

        playerSessionRepository.save(secondPlayerSession);

        List<PlayerSession> result =
                playerSessionRepository.findByStatus(
                        PlayerSessionStatus.ACTIVE
                );
        // Liquibase already inserts ACTIVE sessions for players 1001 and 1002.
        // We are creating another 2 acitve players so total 4
        // in the test setup.
        assertEquals(4, result.size());

        assertTrue(result.stream()
                .allMatch(session ->
                        session.getStatus() == PlayerSessionStatus.ACTIVE));
    }

    @Test
    void findByStatus_shouldReturnEmptyListWhenNoSessionsMatch() {
        playerSession.setStatus(PlayerSessionStatus.NOT_ACTIVE);
        playerSessionRepository.save(playerSession);
        List<PlayerSession> result =
                playerSessionRepository.findByStatus(
                        PlayerSessionStatus.NOT_ACTIVE
                );
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(playerSession.getPlayer().getId(), result.get(0).getPlayer().getId());
    assertEquals( PlayerSessionStatus.NOT_ACTIVE, result.get(0).getStatus() );
    }
}
