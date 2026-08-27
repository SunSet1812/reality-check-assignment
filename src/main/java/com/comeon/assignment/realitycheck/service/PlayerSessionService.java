package com.comeon.assignment.realitycheck.service;

import com.comeon.assignment.realitycheck.dto.PlayerSessionResponseDto;
import com.comeon.assignment.realitycheck.dto.PlayerSessionSchedulerDto;

import java.util.List;

public interface PlayerSessionService {

    String getStatus(long playerId);

    PlayerSessionResponseDto checkUpdateOrCreateSession(
            long playerId,
            int intervalMinutes);

    PlayerSessionResponseDto setAcknowledged(long playerId);

    List<PlayerSessionSchedulerDto> updateActiveSessions();

}
