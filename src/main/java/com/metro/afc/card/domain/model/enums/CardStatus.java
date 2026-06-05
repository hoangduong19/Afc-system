package com.metro.afc.card.domain.model.enums;

public enum CardStatus {
    CREATED, ISSUED, ACTIVE, SUSPENDED, REVOKED;

    public boolean canTransitionTo(CardStatus next) {
        return switch (this) {
            case CREATED   -> next == ISSUED;
            case ISSUED    -> next == ACTIVE;
            case ACTIVE    -> next == SUSPENDED || next == REVOKED;
            case SUSPENDED -> next == ACTIVE    || next == REVOKED;
            case REVOKED   -> false;
        };
    }
}