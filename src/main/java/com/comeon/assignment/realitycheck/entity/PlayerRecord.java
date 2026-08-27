package com.comeon.assignment.realitycheck.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "player")
@Getter
@Setter
public class PlayerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "franchise_id")
    private Long franchiseId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "first_name", length = 64)
    private String firstName;

    @Column(name = "last_name", length = 64)
    private String lastName;

    @Column(name = "gender", length = 16)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "city", length = 64)
    private String city;

    @Column(name = "address", length = 128)
    private String address;

    @Column(name = "postal_code", length = 16)
    private String postalCode;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "language", length = 8)
    private String language;

    @Column(name = "timezone", length = 48)
    private String timezone;

    @Column(name = "registered_at")
    private Instant registeredAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "kyc_status", length = 16)
    private String kycStatus;

    @Column(name = "vip_level")
    private Integer vipLevel;

    @Column(name = "marketing_opt_in")
    private Boolean marketingOptIn;

    @Column(name = "self_excluded")
    private Boolean selfExcluded;

    @Column(name = "deposit_limit_minor")
    private Long depositLimitMinor;

    @Column(name = "balance_minor")
    private Long balanceMinor;

    @Column(name = "bonus_balance_minor")
    private Long bonusBalanceMinor;

    @Column(name = "loyalty_points")
    private Long loyaltyPoints;

    @Column(name = "affiliate_id", length = 32)
    private String affiliateId;

    @Column(name = "referral_code", length = 32)
    private String referralCode;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "account_status", length = 16)
    private String accountStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}