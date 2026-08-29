package com.comeon.assignment.realitycheck.dao;

import com.comeon.assignment.realitycheck.entity.PlayerRecord;
import com.comeon.assignment.realitycheck.repository.PlayerRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PlayerRecordDao {

    private final PlayerRecordRepository playerRecordRepository;

    public PlayerRecord findById(long id) {
        return playerRecordRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("No Player Record found with player id " + id));
    }

}
