package com.comeon.assignment.realitycheck;

import com.comeon.assignment.realitycheck.dao.PlayerRecordDao;
import com.comeon.assignment.realitycheck.dao.PlayerSessionDao;
import com.comeon.assignment.realitycheck.dto.PlayerSessionResponseDto;
import com.comeon.assignment.realitycheck.dto.PlayerSessionSchedulerDto;
import com.comeon.assignment.realitycheck.entity.PlayerAcknowledgement;
import com.comeon.assignment.realitycheck.entity.PlayerRecord;
import com.comeon.assignment.realitycheck.entity.PlayerSession;

import com.comeon.assignment.realitycheck.service.serviceImpl.PlayerSessionServiceImpl;
import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import com.comeon.assignment.realitycheck.util.exception.RealityCheckException;
import com.comeon.assignment.realitycheck.util.mapper.PlayerSessionToDtoMapper;
import com.comeon.assignment.realitycheck.util.mapper.PlayerSessionToSchedulerDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerSessionServiceTest {

    @Mock
    private PlayerSessionDao playerSessionDao;

    @Mock
    private PlayerRecordDao playerRecordDao;

    @Mock
    private PlayerSessionToDtoMapper playerSessionToDtoMapper;

    @Mock
    private PlayerSessionToSchedulerDto playerSessionToSchedulerDto;

    @InjectMocks
    private PlayerSessionServiceImpl playerSessionService;

    // ==================== Player Session Status ====================

    @Test
    public void shouldReturnActiveWhenPlayerHasActiveSession() {
        PlayerSession playerSession = createPlayerSession();
        when(playerSessionDao
                .getActiveSession(1001L, PlayerSessionStatus.ACTIVE))
                .thenReturn(playerSession);
        String responseStatus = playerSessionService.getStatus(1001L);
        assertEquals("Active", responseStatus);
    }

    @Test
    public void shouldThrowExceptionWhenActiveSessionIsNotFound() {
        RuntimeException exception =  new NoSuchElementException("No active player session found associated with playerId: 1001");
        when(playerSessionDao
                .getActiveSession(1001L, PlayerSessionStatus.ACTIVE))
                .thenThrow(exception);
        RuntimeException responseException =
                assertThrows(
                        NoSuchElementException.class,
                        () -> playerSessionService.getStatus(1001L)
                );
        assertEquals(exception.getMessage(), responseException.getMessage());
    }

    // ==================== Active Session Update / Creation ====================

    @Test
    public void shouldSetNewIntervalMinutesWhenActiveSessionIsFound() {
        PlayerRecord playerRecord = createPlayer();
        when(playerRecordDao.findById(1001L)).thenReturn(playerRecord);
        PlayerSession playerSession = createPlayerSession();
        int intervalMinutes = 30;
        when(playerSessionDao.getActiveOptionalSession(1001L)).thenReturn(Optional.of(playerSession));
        handleCheckTimeout(playerSession, intervalMinutes);
        when(playerSessionDao.savePlayerSession(
                playerSession, playerSession.getPlayer().getId()))
                .thenReturn(playerSession);
        PlayerSessionResponseDto expectedPlayerSessionDto = toPlayerSessionResponseDto(playerSession);
        when(playerSessionToDtoMapper.toDto(
                playerSession,
                playerSession.getPlayer().getTimezone()))
                .thenReturn(expectedPlayerSessionDto);
        PlayerSessionResponseDto responsePlayerSessionDto = playerSessionService
                .checkUpdateOrCreateSession(1001L, 30);
        assertEquals(expectedPlayerSessionDto, responsePlayerSessionDto);
    }

    @Test
    public void shouldResetAcknowledgementWhenSessionTimeoutIsReachedForActiveSession() {
        PlayerRecord playerRecord = createPlayer();
        when(playerRecordDao.findById(1001L)).thenReturn(playerRecord);
        PlayerSession playerSession = createPlayerSession();
        Instant t = Instant.now().minus(25, ChronoUnit.MINUTES);
        playerSession.setStartedAt(t);
        playerSession.setLastPromptAt(t);
        playerSession.setNextCheckAt(t.plus(20, ChronoUnit.MINUTES));
        int intervalMinutes = 30;
        when(playerSessionDao.getActiveOptionalSession(1001L)).thenReturn(Optional.of(playerSession));
        handleCheckTimeout(playerSession, intervalMinutes);
        when(playerSessionDao.savePlayerSession(
                playerSession, playerSession.getPlayer().getId()))
                .thenReturn(playerSession);
        PlayerSessionResponseDto expectedPlayerSessionDto = toPlayerSessionResponseDto(playerSession);
        when(playerSessionToDtoMapper.toDto(
                playerSession,
                playerSession.getPlayer().getTimezone()))
                .thenReturn(expectedPlayerSessionDto);
        PlayerSessionResponseDto responsePlayerSessionDto = playerSessionService
                .checkUpdateOrCreateSession(1001L, 30);
        assertEquals(expectedPlayerSessionDto, responsePlayerSessionDto);
    }

    @Test
    public void shouldThrowExceptionWhenPlayerAndSessionFranchiseDoNotMatch() {
        PlayerRecord playerRecord = createPlayer();
        when(playerRecordDao.findById(1001L)).thenReturn(playerRecord);
        PlayerSession playerSession = createPlayerSession();
        playerSession.setFranchiseId(22);
        int intervalMinutes = 30;
        when(playerSessionDao.getActiveOptionalSession(1001L))
                .thenReturn(Optional.of(playerSession));
        RuntimeException expectedException =  new RealityCheckException("FRANCHISE_MISMATCH");
        RuntimeException responseException = assertThrows(RealityCheckException.class,
                () -> playerSessionService.checkUpdateOrCreateSession(1001L, intervalMinutes));
        assertEquals(expectedException.getMessage(), responseException.getMessage());
    }

    @Test
    public void shouldCreateNewPlayerSessionWhenNoActiveSessionFound() {
        PlayerRecord playerRecord = createPlayer();
        when(playerRecordDao.findById(1001L)).thenReturn(playerRecord);
        when(playerSessionDao.getActiveOptionalSession(1001L))
                .thenReturn(Optional.empty());
        PlayerSession playerSession = createPlayerSession();
        when(playerSessionDao.savePlayerSession(any(PlayerSession.class), eq(1001L)))
                .thenReturn(playerSession);
        PlayerSessionResponseDto expectedPlayerSessionDto = toPlayerSessionResponseDto(playerSession);
        when(playerSessionToDtoMapper.toDto(playerSession, playerSession.getPlayer().getTimezone()))
                .thenReturn(expectedPlayerSessionDto);
        int intervalMinutes = 20;
        PlayerSessionResponseDto responsePlayerSessionDto = playerSessionService
                .checkUpdateOrCreateSession(playerSession.getPlayer().getId(), intervalMinutes);
        assertEquals(expectedPlayerSessionDto, responsePlayerSessionDto);
    }

    // ==================== Set Acknowledgment ====================

    @Test
    public void shouldAcknowledgeActivePlayerSession() {
        PlayerSession playerSession = createPlayerSession();
        when(playerSessionDao
                .getActiveSession(1001L, PlayerSessionStatus.ACTIVE))
                .thenReturn(playerSession);
        playerSession.setAcknowledged(true);
        PlayerAcknowledgement playerAcknowledgement =
                PlayerAcknowledgement.builder()
                        .acknowledgedAt(Instant.now())
                        .playerSession(playerSession)
                        .build();
        if(playerSession.getAcknowledgementList() == null || playerSession.getAcknowledgementList().isEmpty()) {
            List<PlayerAcknowledgement> playerAcknowledgments = new ArrayList<>();
            playerAcknowledgments.add(playerAcknowledgement);
            playerSession.setAcknowledgementList(playerAcknowledgments);
        }
        else {
            playerSession.getAcknowledgementList()
                    .add(playerAcknowledgement);
        }
        when(playerSessionDao
                .savePlayerSession(playerSession, 1001L))
                .thenReturn(playerSession);
        PlayerSessionResponseDto expectedPlayerSessionDto = toPlayerSessionResponseDto(playerSession);
        when(playerSessionToDtoMapper.toDto(playerSession, playerSession.getPlayer().getTimezone()))
                .thenReturn(expectedPlayerSessionDto);
        PlayerSessionResponseDto playerSessionResponseDto = playerSessionService.setAcknowledged(1001L);
        assertEquals(expectedPlayerSessionDto, playerSessionResponseDto);
    }

    @Test
    public void shouldThrowExceptionIfActiveSessionIsNotFound() {
        RuntimeException exception =  new NoSuchElementException("No active player session found associated with playerId: 1001");
        when(playerSessionDao
                .getActiveSession(1001L, PlayerSessionStatus.ACTIVE))
                .thenThrow(exception);
        RuntimeException responseException =
                assertThrows(
                        NoSuchElementException.class,
                        () -> playerSessionService.setAcknowledged(1001L)
                );
        assertEquals(exception.getMessage(), responseException.getMessage());
    }

    // ==================== Set Acknowledgment ====================

    @Test
    public void shouldUpdateAllActiveSessionsAndReturnSchedulerDtos() {

        PlayerSession playerSession1 = createPlayerSession();
        PlayerSession playerSession2 = createPlayerSession();

        playerSession2.setId(1002L);

        when(playerSessionDao.getAllActivePlayerSessions())
                .thenReturn(List.of(playerSession1, playerSession2));

        updateActiveSession(playerSession1);
        updateActiveSession(playerSession2);

        when(playerSessionDao.savePlayerSession(
                eq(playerSession1),
                eq(playerSession1.getPlayer().getId())))
                .thenReturn(playerSession1);

        when(playerSessionDao.savePlayerSession(
                eq(playerSession2),
                eq(playerSession2.getPlayer().getId())))
                .thenReturn(playerSession2);

        PlayerSessionSchedulerDto expectedPlayerSessionSchedulerDto1
                = toPlayerSessionSchedulerDto(playerSession1);
        PlayerSessionSchedulerDto expectedPlayerSessionSchedulerDto2
                = toPlayerSessionSchedulerDto(playerSession2);

        when(playerSessionToSchedulerDto.toDto(playerSession1))
                .thenReturn(expectedPlayerSessionSchedulerDto1);

        when(playerSessionToSchedulerDto.toDto(playerSession2))
                .thenReturn(expectedPlayerSessionSchedulerDto2);

        List<PlayerSessionSchedulerDto> response =
                playerSessionService.updateActiveSessions();

        assertEquals(2, response.size());
        assertEquals(expectedPlayerSessionSchedulerDto1, response.get(0));
        assertEquals(expectedPlayerSessionSchedulerDto2, response.get(1));
    }

    private void updateActiveSession(
            PlayerSession playerSession
    ) {
        Instant now = Instant.now();
        playerSession.setElapsedSeconds(
                Duration.between(
                        playerSession.getStartedAt(),
                        now
                ).getSeconds()
        );
        if (!now.isBefore(playerSession.getNextCheckAt())) {
            playerSession.setAcknowledged(false);
            playerSession.setLastPromptAt(now);
            playerSession.setNextCheckAt(
                    now.plus(
                            (long) playerSession.getIntervalMinutes() * 60,
                            ChronoUnit.SECONDS
                    )
            );
        }
    }

    private void handleCheckTimeout(
            PlayerSession playerSession,
            int intervalMinutes
    ) {
        Instant now = Instant.now();

        long checkTime =
                Duration.between(
                        playerSession.getStartedAt(),
                        now
                ).getSeconds();

        playerSession.setElapsedSeconds(checkTime);
        playerSession.setIntervalMinutes(intervalMinutes);

        if (!now.isBefore(playerSession.getNextCheckAt())) {
            playerSession.setAcknowledged(false);
            playerSession.setLastPromptAt(now);
            playerSession.setNextCheckAt(
                    now.plus(intervalMinutes, ChronoUnit.MINUTES)
            );
        }
    }


    private PlayerSession createPlayerSession() {

        PlayerRecord playerRecord = createPlayer();
        Instant t = Instant.now().minus(15, ChronoUnit.MINUTES);
        return PlayerSession
                .builder()
                .id(1L)
                .franchiseId(playerRecord.getFranchiseId())
                .player(playerRecord)
                .status(PlayerSessionStatus.ACTIVE)
                .intervalMinutes(20)
                .startedAt(t)
                .lastPromptAt(t)
                .elapsedSeconds(0L)
                .netAmountMinor(0L)
                .acknowledged(false)
                .nextCheckAt(t.plus(20, ChronoUnit.MINUTES))
                .build();
    }

    private PlayerRecord createPlayer() {
        return PlayerRecord
                .builder()
                .id(1001L)
                .franchiseId(10L)
                .timezone("Europe/Stockholm")
                .build();
    }

    private PlayerSessionResponseDto toPlayerSessionResponseDto(PlayerSession playerSession) {
        PlayerRecord player = playerSession.getPlayer();

        return PlayerSessionResponseDto.builder()
                .id(playerSession.getId())
                .playerId(player.getId())
                .franchiseId(playerSession.getFranchiseId())
                .status(playerSession.getStatus())
                .intervalMinutes(playerSession.getIntervalMinutes())
                .startedAt(formatInstant(
                        playerSession.getStartedAt(),
                        player.getTimezone()
                ))
                .lastPromptAt(formatInstant(
                        playerSession.getLastPromptAt(),
                        player.getTimezone()
                ))
                .elapsedSeconds(playerSession.getElapsedSeconds())
                .netAmountMinor(playerSession.getNetAmountMinor())
                .acknowledged(playerSession.getAcknowledged())
                .nextCheckAt(formatInstant(
                        playerSession.getNextCheckAt(),
                        player.getTimezone()
                ))
                .build();
    }
    private String formatInstant(Instant instant, String timezone) {
        if (instant == null) {
            return null;
        }

        return instant
                .atZone(ZoneId.of(timezone))
                .toString();
    }

    private PlayerSessionSchedulerDto toPlayerSessionSchedulerDto(PlayerSession playerSession) {
        return PlayerSessionSchedulerDto.builder()
                .playerId(playerSession.getPlayer().getId())
                .franchiseId(playerSession.getFranchiseId())
                .intervalMinutes(playerSession.getIntervalMinutes())
                .elapsedSeconds(playerSession.getElapsedSeconds())
                .netAmountMinor(playerSession.getNetAmountMinor())
                .lastPromptAt(playerSession.getLastPromptAt())
                .nextCheckAt(playerSession.getNextCheckAt())
                .build();
    }

}
