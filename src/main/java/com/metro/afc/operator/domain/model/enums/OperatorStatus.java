package com.metro.afc.operator.domain.model.enums;

public enum OperatorStatus {
    ACTIVE, INACTIVE;
    public boolean isActive() {
        return this == ACTIVE;
    }
}