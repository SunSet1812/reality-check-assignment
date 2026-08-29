package com.comeon.assignment.realitycheck.entity;

import com.comeon.assignment.realitycheck.util.enums.PlayerSessionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "player_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerRecord player;

    @Column(name = "franchise_id")
    private long franchiseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16)
    private PlayerSessionStatus status;

    @Column(name = "interval_minutes")
    private Integer intervalMinutes;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "last_prompt_at")
    private Instant lastPromptAt;

    @Column(name = "elapsed_seconds")
    private Long elapsedSeconds;

    @Column(name = "net_amount_minor")
    private Long netAmountMinor;

    @Column(name = "acknowledged")
    private Boolean acknowledged;

    @Column(name = "next_check_at")
    private Instant nextCheckAt;

    @OneToMany(mappedBy = "playerSession", cascade = CascadeType.ALL)
    private List<PlayerAcknowledgement> acknowledgementList;
}