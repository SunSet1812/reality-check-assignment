package com.comeon.assignment.realitycheck.job;

import com.comeon.assignment.realitycheck.dto.PlayerSessionSchedulerDto;
import com.comeon.assignment.realitycheck.service.serviceImpl.PlayerSessionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayerSessionSchedulerJob {

    private final PlayerSessionServiceImpl playerSessionService;

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(
            name = "playerSessionRefreshJob",
            lockAtMostFor = "2m",
            lockAtLeastFor = "45s"
    )
    public void run(){
        List<PlayerSessionSchedulerDto> playerSessionsDto = playerSessionService.updateActiveSessions();
        playerSessionsDto.forEach(playerSessionSchedulerDto ->
                log.info("Sending reality check event: {}",
                        playerSessionSchedulerDto
        ));
    }
}
