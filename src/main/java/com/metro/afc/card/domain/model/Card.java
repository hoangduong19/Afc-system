package com.metro.afc.card.domain.model;

import com.metro.afc.card.domain.model.enums.CardStatus;
import com.metro.afc.card.domain.model.enums.CardType;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    private boolean supportsMetro;

    @Column(name = "supports_bus", nullable = false)
    private boolean supportsBus;

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

    // ── Factory method ───────────────────────────────────────────

    public static Card create(String cardUid, boolean supportsMetro, boolean supportsBus) {
        Card card = new Card();
        card.cardUid       = cardUid;
        card.status        = CardStatus.CREATED;
        card.type          = CardType.ANON;
        card.supportsMetro = supportsMetro;
        card.supportsBus   = supportsBus;
        return card;
    }

    // ── State machine ────────────────────────────────────────────

    public void issue(UUID stationId) {
        transitionTo(CardStatus.ISSUED);
        this.issuedAtStationId = stationId;
    }

    public void activate() {
        transitionTo(CardStatus.ACTIVE);
        this.activatedAt = Instant.now();
    }

    public void suspend() {
        transitionTo(CardStatus.SUSPENDED);
    }

    public void unsuspend() {
        transitionTo(CardStatus.ACTIVE);
    }

    public void revoke() {
        transitionTo(CardStatus.REVOKED);
    }

    // ── Domain behavior ──────────────────────────────────────────

    public boolean isActive() {
        return this.status == CardStatus.ACTIVE;
    }

    // ── Private ──────────────────────────────────────────────────

    private void transitionTo(CardStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new BusinessRuleException(
                    ErrorCode.CARD_INVALID_TRANSITION,
                    "Không thể chuyển từ %s sang %s".formatted(this.status, next)
            );
        }
        this.status = next;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}