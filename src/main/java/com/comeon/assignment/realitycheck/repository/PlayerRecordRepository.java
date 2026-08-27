package com.comeon.assignment.realitycheck.repository;

import com.comeon.assignment.realitycheck.entity.PlayerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRecordRepository extends JpaRepository<PlayerRecord, Long> {
}
