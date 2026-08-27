package com.comeon.assignment.realitycheck.util.mapper;

import com.comeon.assignment.realitycheck.dto.PlayerAcknowledgementResponseDto;
import com.comeon.assignment.realitycheck.dto.PlayerSessionResponseDto;
import com.comeon.assignment.realitycheck.entity.PlayerAcknowledgement;
import com.comeon.assignment.realitycheck.entity.PlayerSession;
import com.comeon.assignment.realitycheck.util.DateTimeFormatterUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


import java.time.Instant;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlayerSessionToDtoMapper {

    @Mapping(target = "playerId", source = "player.id")
    PlayerSessionResponseDto toDto(
            PlayerSession session,
            @Context String timezone
    );

    @Mapping(target = "acknowledgedAt", source = "acknowledgedAt")
    PlayerAcknowledgementResponseDto toAcknowledgementDto(
            PlayerAcknowledgement acknowledgement,
            @Context String timezone
    );

    default String mapInstant(
            Instant timestamp,
            @Context String timezone
    ) {
        return DateTimeFormatterUtil.format(timestamp, timezone);
    }
}