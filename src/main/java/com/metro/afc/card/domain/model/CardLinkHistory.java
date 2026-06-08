package com.metro.afc.card.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_link_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardLinkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "card_id", nullable = false, columnDefinition = "uuid")
    private UUID cardId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "merged_balance", precision = 15, scale = 2)
    private BigDecimal mergedBalance;

    @Column(name = "performed_by", columnDefinition = "uuid")
    private UUID performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    public static CardLinkHistory linked(UUID cardId, UUID userId, UUID performedBy) {
        CardLinkHistory h = new CardLinkHistory();
        h.cardId      = cardId;
        h.userId      = userId;
        h.action      = "LINK";
        h.performedBy = performedBy;
        h.performedAt = Instant.now();
        return h;
    }

    public static CardLinkHistory unlinked(UUID cardId, UUID userId, UUID performedBy) {
        CardLinkHistory h = new CardLinkHistory();
        h.cardId      = cardId;
        h.userId      = userId;
        h.action      = "UNLINK";
        h.performedBy = performedBy;
        h.performedAt = Instant.now();
        return h;
    }
}