package com.metro.afc.card.domain.model;

import com.metro.afc.card.domain.events.cardLink.CardLinkedEvent;
import com.metro.afc.card.domain.events.cardStatus.CardStatusChangedEvent;
import com.metro.afc.card.domain.events.cardLink.CardUnlinkedEvent;
import com.metro.afc.card.domain.model.enums.CardStatus;
import com.metro.afc.card.domain.model.enums.CardType;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends AbstractAggregateRoot<Card> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "card_uid", nullable = false, unique = true, length = 100)
    private String cardUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType type;

    @Column(name = "supports_metro", nullable = false)
    private Boolean supportsMetro;

    @Column(name = "supports_bus", nullable = false)
    private Boolean supportsBus;

    @Column(name = "issued_at_station_id", columnDefinition = "uuid")
    private UUID issuedAtStationId;

    @Column(name = "linked_user_id", columnDefinition = "uuid")
    private UUID linkedUserId;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "linked_at")
    private Instant linkedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Factory ──────────────────────────────────────────────────

    public static Card create(String cardUid, CardType type, UUID userId,
                              Boolean supportsMetro, Boolean supportsBus,
                              UUID createdBy) {
        Card card          = new Card();
        card.id            = UUID.randomUUID();
        card.cardUid       = cardUid.trim().toUpperCase();
        card.status        = CardStatus.CREATED;
        card.type          = type;
        card.linkedUserId  = userId;
        card.linkedAt      = userId != null ? Instant.now() : null;
        card.supportsMetro = supportsMetro;
        card.supportsBus   = supportsBus;
        card.registerEvent(new CardStatusChangedEvent(
                card.id, null, CardStatus.CREATED, "Card created", createdBy
        ));

        if (type == CardType.IDENTIFIED) {
            card.status      = CardStatus.ISSUED;
            card.registerEvent(new CardStatusChangedEvent(
                    card.id, CardStatus.CREATED, CardStatus.ISSUED,
                    "Auto-issued for identified card", createdBy
            ));

            card.status      = CardStatus.ACTIVE;
            card.activatedAt = Instant.now();
            card.registerEvent(new CardStatusChangedEvent(
                    card.id, CardStatus.ISSUED, CardStatus.ACTIVE,
                    "Auto-activated for identified card", createdBy
            ));
        }
        return card;
    }

    // ── State transitions ────────────────────────────────────────

    public void issue(UUID stationId, UUID changedBy) {
        validateTransition(CardStatus.ISSUED);
        CardStatus prev      = this.status;
        this.status          = CardStatus.ISSUED;
        this.issuedAtStationId = stationId;
        this.registerEvent(new CardStatusChangedEvent(
                id, prev, CardStatus.ISSUED, null, changedBy
        ));
    }

    public void activate(UUID changedBy) {
        validateTransition(CardStatus.ACTIVE);
        CardStatus prev  = this.status;
        this.status      = CardStatus.ACTIVE;
        this.activatedAt = Instant.now();
        this.registerEvent(new CardStatusChangedEvent(
                id, prev, CardStatus.ACTIVE, null, changedBy
        ));
    }

    public void suspend(String reason, UUID changedBy) {
        validateTransition(CardStatus.SUSPENDED);
        CardStatus prev = this.status;
        this.status     = CardStatus.SUSPENDED;
        this.registerEvent(new CardStatusChangedEvent(
                id, prev, CardStatus.SUSPENDED, reason, changedBy
        ));
    }

    public void unsuspend(String reason, UUID changedBy) {
        validateTransition(CardStatus.ACTIVE);
        CardStatus prev = this.status;
        this.status     = CardStatus.ACTIVE;
        this.registerEvent(new CardStatusChangedEvent(
                id, prev, CardStatus.ACTIVE, reason, changedBy
        ));
    }

    public void revoke(String reason, UUID changedBy) {
        validateTransition(CardStatus.REVOKED);
        CardStatus prev = this.status;
        this.status     = CardStatus.REVOKED;
        this.registerEvent(new CardStatusChangedEvent(
                id, prev, CardStatus.REVOKED, reason, changedBy
        ));
    }

    private void validateTransition(CardStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new BusinessRuleException(
                    ErrorCode.CARD_INVALID_TRANSITION,
                    "Cannot transition from " + this.status + " to " + target
            );
        }
    }

    public void link(UUID userId, UUID performedBy) {
        if (this.linkedUserId != null) {
            throw new BusinessRuleException(
                    ErrorCode.CARD_ALREADY_LINKED, "Card is already linked to a user"
            );
        }
        if (this.status != CardStatus.ACTIVE) {
            throw new BusinessRuleException(
                    ErrorCode.CARD_INVALID_TRANSITION, "Card must be ACTIVE to link"
            );
        }
        this.linkedUserId = userId;
        this.linkedAt     = Instant.now();
        this.type         = CardType.IDENTIFIED;
        this.registerEvent(new CardLinkedEvent(id, userId, performedBy));
    }

    public void unlink(UUID performedBy) {
        if (this.linkedUserId == null) {
            throw new BusinessRuleException(
                    ErrorCode.CARD_NOT_LINKED, "Card is not linked to any user"
            );
        }
        UUID previousUserId = this.linkedUserId;
        this.linkedUserId   = null;
        this.linkedAt       = null;
        this.type           = CardType.ANON;
        this.registerEvent(new CardUnlinkedEvent(id, previousUserId, performedBy));
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}