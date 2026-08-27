package com.comeon.assignment.realitycheck.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "player_session_acknowledgement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAcknowledgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Instant acknowledgedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_session_id", nullable = false)
    @JsonIgnore
    private PlayerSession playerSession;
}