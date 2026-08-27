package com.comeon.assignment.realitycheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSessionSchedulerDto {

    private long playerId;
    private long franchiseId;
    private int intervalMinutes;
    private long elapsedSeconds;
    private long netAmountMinor;
    private Instant lastPromptAt;
    private Instant nextCheckAt;
}