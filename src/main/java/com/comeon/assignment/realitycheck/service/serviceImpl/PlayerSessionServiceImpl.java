package com.comeon.assignment.realitycheck.service.serviceImpl;

import com.comeon.assignment.realitycheck.dao.PlayerRecordDao;
import com.comeon.assignment.realitycheck.dao.PlayerSessionDao;
import com.comeon.assignment.realitycheck.dto.PlayerSessionResponseDto;
import com.comeon.assignment.realitycheck.dto.PlayerSessionSchedulerDto;
import com.comeon.assignment.realitycheck.entity.PlayerAcknowledgement;
import com.comeon.assignment.realitycheck.entity.PlayerRecord;
import com.comeon.assignment.realitycheck.entity.PlayerSession;
import com.comeon.assignment.realitycheck.service.PlayerSessionService;
import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import com.comeon.assignment.realitycheck.util.exception.RealityCheckException;
import com.comeon.assignment.realitycheck.util.mapper.PlayerSessionToDtoMapper;
import com.comeon.assignment.realitycheck.util.mapper.PlayerSessionToSchedulerDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus.ACTIVE;

@Service
@RequiredArgsConstructor
public class PlayerSessionServiceImpl implements PlayerSessionService {

    private final PlayerSessionDao playerSessionDao;
    private final PlayerRecordDao playerRecordDao;
    private final PlayerSessionToDtoMapper playerSessionToDtoMapper;
    private final PlayerSessionToSchedulerDto playerSessionToSchedulerDto;

    @Transactional
    public String getStatus(long playerId) {
        PlayerSession playerSession =
                playerSessionDao.getActiveSession(
                        playerId,
                        PlayerSessionStatus.ACTIVE
                );

        return playerSession.getStatus().toString();
    }

    private PlayerSession createSession(
            PlayerRecord playerRecord,
            int intervalMinutes
    ) {
        Instant now = Instant.now();

        PlayerSession tmp = PlayerSession.builder()
                .player(playerRecord)
                .franchiseId(playerRecord.getFranchiseId())
                .status(ACTIVE)
                .intervalMinutes(intervalMinutes)
                .startedAt(now)
                .lastPromptAt(now)
                .elapsedSeconds(0L)
                .netAmountMinor(0L)
                .acknowledged(false)
                .nextCheckAt(now.plus(intervalMinutes, ChronoUnit.MINUTES))
                .build();

        return playerSessionDao.savePlayerSession(
                tmp,
                playerRecord.getId()
        );
    }

    @Transactional
    public PlayerSessionResponseDto checkUpdateOrCreateSession(
            long playerId,
            int intervalMinutes
    ) {
        PlayerRecord playerRecord =
                playerRecordDao.findById(playerId);

        Optional<PlayerSession> optionalPlayerSession =
                playerSessionDao.getActiveOptionalSession(playerId);

        if (optionalPlayerSession.isEmpty()) {
            PlayerSession createdPlayerSession =
                    createSession(playerRecord, intervalMinutes);

            return playerSessionToDtoMapper.toDto(
                    createdPlayerSession,
                    createdPlayerSession.getPlayer().getTimezone()
            );
        }

        PlayerSession playerSession = optionalPlayerSession.get();

        if (playerSession.getFranchiseId()
                != playerRecord.getFranchiseId()) {

            throw new RealityCheckException("FRANCHISE_MISMATCH");
        }

        PlayerSession updatedPlayerResponse =
                updateSession(playerSession, intervalMinutes);

        return playerSessionToDtoMapper.toDto(
                updatedPlayerResponse,
                updatedPlayerResponse.getPlayer().getTimezone()
        );
    }

    private PlayerSession updateSession(
            PlayerSession playerSession,
            int intervalMinutes
    ) {
        handleCheckTimeout(playerSession, intervalMinutes);

        return playerSessionDao.savePlayerSession(
                playerSession, playerSession.getPlayer().getId()

        );
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

    @Transactional
    public PlayerSessionResponseDto setAcknowledged(long playerId) {
        PlayerSession playerSession =
                playerSessionDao.getActiveSession(
                        playerId,
                        PlayerSessionStatus.ACTIVE
                );
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
        PlayerSession playerResponse =
                playerSessionDao.savePlayerSession(
                        playerSession,
                        playerId
                );
        return playerSessionToDtoMapper.toDto(
                playerResponse,
                playerResponse.getPlayer().getTimezone()
        );
    }

    @Transactional
    public List<PlayerSessionSchedulerDto> updateActiveSessions() {
        List<PlayerSession> activePlayerSessions =
                playerSessionDao.getAllActivePlayerSessions();
        List<PlayerSessionSchedulerDto> playerSessionsDto =
                new ArrayList<>();
        for (PlayerSession playerSession : activePlayerSessions) {
            PlayerSession updatedSession =
                    updateActiveSession(playerSession);
            playerSessionsDto.add(
                    playerSessionToSchedulerDto.toDto(updatedSession)
            );
        }

        return playerSessionsDto;
    }

    private PlayerSession updateActiveSession(
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
        return playerSessionDao.savePlayerSession(
                playerSession,
                playerSession.getPlayer().getId()
        );
    }
}
