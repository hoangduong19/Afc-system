package com.metro.afc.card.domain.model;

import com.metro.afc.card.domain.model.enums.CardStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "card_id", nullable = false, columnDefinition = "uuid")
    private UUID cardId;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 20)
    private String toStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_by", columnDefinition = "uuid")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    public static CardStatusHistory of(UUID cardId, CardStatus fromStatus,
                                       CardStatus toStatus, String reason,
                                       UUID changedBy) {
        CardStatusHistory h = new CardStatusHistory();
        h.cardId     = cardId;
        h.fromStatus = fromStatus != null ? fromStatus.name() : null;
        h.toStatus   = toStatus.name();
        h.reason     = reason;
        h.changedBy  = changedBy;
        h.changedAt  = Instant.now();
        return h;
    }
}