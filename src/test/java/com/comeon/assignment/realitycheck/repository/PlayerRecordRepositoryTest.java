package com.comeon.assignment.realitycheck.repository;


import com.comeon.assignment.realitycheck.entity.PlayerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class  PlayerRecordRepositoryTest {

    @Autowired
    private PlayerRecordRepository playerRecordRepository;

    private PlayerRecord playerRecord;

    @BeforeEach
    void setUp() {
        playerRecord = PlayerRecord.builder()
                .franchiseId(10L)
                .timezone("Europe/Stockholm")
                .build();

    }

    @Test
    void testFindById_whenValidIdIsPresent_shouldReturnPlayerRecord() {
        PlayerRecord savedPlayerRecord = playerRecordRepository.save(playerRecord);
        Optional<PlayerRecord> playerRecordOptional = playerRecordRepository.findById(savedPlayerRecord.getId());
        PlayerRecord responsePlayerRecord = null;
        if (playerRecordOptional.isPresent()) {
            responsePlayerRecord = playerRecordOptional.get();
        }
        assertNotNull(responsePlayerRecord);
        assertEquals(responsePlayerRecord.getFranchiseId(), savedPlayerRecord.getFranchiseId());
        assertEquals(responsePlayerRecord.getTimezone(), savedPlayerRecord.getTimezone());
    }

    @Test
    void testFindById_whenInvalidIdIsPresent_shouldReturnNull() {
        Optional<PlayerRecord> playerRecordOptional = playerRecordRepository.findById(2000L);
        assertTrue(playerRecordOptional.isEmpty());
    }
}
