package com.comeon.assignment.realitycheck.controller;

import com.comeon.assignment.realitycheck.dto.PlayerSessionResponseDto;
import com.comeon.assignment.realitycheck.service.serviceImpl.PlayerSessionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlayerSessionController {

    private final PlayerSessionServiceImpl playerSessionService;

    @GetMapping("/getStatus/{playerId}")
    public ResponseEntity<String> getStatus(@PathVariable("playerId") long playerId) {
        String status = playerSessionService.getStatus(playerId);
        return new  ResponseEntity<>(status, HttpStatus.OK);
    }

    @PostMapping("/{playerId}/{intervalMinutes}")
    public ResponseEntity<PlayerSessionResponseDto> checkSessionUpdateOrCreate(@PathVariable("playerId") long playerId, @PathVariable("intervalMinutes") int intervalMinutes) {
        PlayerSessionResponseDto playerSessionResponse= playerSessionService.checkUpdateOrCreateSession(playerId, intervalMinutes);
        return new ResponseEntity<>(playerSessionResponse, HttpStatus.OK);
    }

    @PutMapping("/acknowledge/{playerId}")
    public ResponseEntity<PlayerSessionResponseDto> acknowledge(@PathVariable("playerId") long playerId) {
        PlayerSessionResponseDto playerSession = playerSessionService.setAcknowledged(playerId);
        return new ResponseEntity<>(playerSession, HttpStatus.OK);
    }

}
