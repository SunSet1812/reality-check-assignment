package com.comeon.assignment.realitycheck.util.mapper;

import com.comeon.assignment.realitycheck.dto.PlayerSessionSchedulerDto;
import com.comeon.assignment.realitycheck.entity.PlayerSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlayerSessionToSchedulerDto {

    @Mapping(target = "playerId", source = "player.id")
    PlayerSessionSchedulerDto toDto(PlayerSession session);
}