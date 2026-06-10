package com.metro.afc.ticket.domain;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import com.metro.afc.ticket.domain.events.TicketCreatedEvent;
import com.metro.afc.ticket.domain.events.TicketLinkedToCardEvent;
import com.metro.afc.ticket.domain.events.TicketUnlinkedFromCardEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends AbstractAggregateRoot<Ticket> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "card_id", columnDefinition = "uuid")
    private UUID cardId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType type;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "price", nullable = false, precision = 15, scale = 2))
    private Money price;

    @Column(name = "fare_rule_id", columnDefinition = "uuid")
    private UUID fareRuleId;

    @Column(name = "discount_id", columnDefinition = "uuid")
    private UUID discountId;

    @Column(name = "from_station_id", columnDefinition = "uuid")
    private UUID fromStationId;

    @Column(name = "to_station_id", columnDefinition = "uuid")
    private UUID toStationId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FareMode mode;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private Instant purchasedAt;

    @Column(name = "used_at")
    private Instant usedAt;

    // ── Factory: Single Trip ─────────────────────────────────────

    public static Ticket createSingleTrip(UUID userId,
                                          UUID fromStationId, UUID toStationId,
                                          FareMode mode, Money price,
                                          UUID fareRuleId, UUID discountId) {
        Ticket t        = new Ticket();
        t.id            = UUID.randomUUID();
        t.userId        = userId;
        t.cardId        = null;
        t.type          = TicketType.SINGLE_TRIP;
        t.price         = price;
        t.fareRuleId    = fareRuleId;
        t.discountId    = discountId;
        t.fromStationId = fromStationId;
        t.toStationId   = toStationId;
        t.mode          = mode;
        t.validFrom     = LocalDate.now();
        t.validTo       = LocalDate.now().plusDays(1);
        t.status        = TicketStatus.ACTIVE;
        t.registerEvent(new TicketCreatedEvent(t));
        return t;
    }

    // ── Factory: Monthly Pass ────────────────────────────────────

    public static Ticket createMonthlyPass(UUID userId, FareMode mode,
                                           Money price, UUID fareRuleId,
                                           UUID discountId, LocalDate validFrom,
                                           int durationDays) {
        Ticket t     = new Ticket();
        t.id         = UUID.randomUUID();
        t.userId     = userId;
        t.cardId     = null;
        t.type       = TicketType.MONTHLY_PASS;
        t.price      = price;
        t.fareRuleId = fareRuleId;
        t.discountId = discountId;
        t.mode       = mode;
        t.validFrom  = validFrom;
        t.validTo    = validFrom.plusDays(durationDays);
        t.status     = TicketStatus.ACTIVE;
        t.registerEvent(new TicketCreatedEvent(t));
        return t;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public void markUsed() {
        if (this.status != TicketStatus.ACTIVE)
            throw new BusinessRuleException(
                    ErrorCode.TICKET_INVALID_STATUS,
                    "Ticket is not ACTIVE"
            );
        this.status = TicketStatus.USED;
        this.usedAt = Instant.now();
    }

    public void linkToCard(UUID cardId) {
        if (this.type != TicketType.MONTHLY_PASS)
            throw new BusinessRuleException(
                    ErrorCode.TICKET_INVALID_STATUS,
                    "Only MONTHLY_PASS can be linked to a card"
            );
        if (this.cardId != null)
            throw new BusinessRuleException(
                    ErrorCode.TICKET_ALREADY_LINKED,
                    "Ticket already linked to a card"
            );
        this.cardId = cardId;
        this.registerEvent(new TicketLinkedToCardEvent(this));
    }

    public void unlinkFromCard() {
        if (this.cardId == null)
            throw new BusinessRuleException(
                    ErrorCode.TICKET_INVALID_STATUS,
                    "Ticket is not linked to any card"
            );
        UUID previousCardId = this.cardId;
        this.cardId = null;
        this.registerEvent(new TicketUnlinkedFromCardEvent(this.id, previousCardId));
    }

    @PrePersist
    protected void onCreate() { purchasedAt = Instant.now(); }
}