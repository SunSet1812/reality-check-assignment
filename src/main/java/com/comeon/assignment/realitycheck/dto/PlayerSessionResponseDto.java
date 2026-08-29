package com.comeon.assignment.realitycheck.dto;

import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSessionResponseDto {
    Long id;
    Long playerId;
    long franchiseId;
    PlayerSessionStatus status;
    Integer intervalMinutes;
    String startedAt;
    String lastPromptAt;
    Long elapsedSeconds;
    Long netAmountMinor;
    Boolean acknowledged;
    String acknowledgedAt;
    String nextCheckAt;
    private List<PlayerAcknowledgementResponseDto> acknowledgementList;
}

