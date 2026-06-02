package com.metro.afc.identity.domain.model.enums;

public enum UserStatus {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
