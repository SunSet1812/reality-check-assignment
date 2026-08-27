package com.comeon.assignment.realitycheck.util.enums;

public enum PlayerSessionStatus {

    ACTIVE("Active"),
    NOT_ACTIVE("Not_active");

    private final String value;

    PlayerSessionStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}