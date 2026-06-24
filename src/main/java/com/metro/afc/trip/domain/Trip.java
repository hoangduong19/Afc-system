package com.metro.afc.trip.domain;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "external_transaction_id",
            nullable = false, unique = true, columnDefinition = "uuid")
    private UUID externalTransactionId;

    @Column(name = "card_id", columnDefinition = "uuid")
    private UUID cardId;

    @Column(name = "operator_id", columnDefinition = "uuid")
    private UUID operatorId;

    @Column(name = "tap_in_station_id", nullable = false, columnDefinition = "uuid")
    private UUID tapInStationId;

    @Column(name = "tap_in_at", nullable = false)
    private Instant tapInAt;

    @Column(name = "tap_out_station_id", columnDefinition = "uuid")
    private UUID tapOutStationId;

    @Column(name = "tap_out_at")
    private Instant tapOutAt;

    @Column(name = "distance_km", precision = 10, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "fare_amount", precision = 15, scale = 2)
    private BigDecimal fareAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type_used", length = 20)
    private TicketTypeUsed ticketTypeUsed;

    @Column(name = "ticket_id", columnDefinition = "uuid")
    private UUID ticketId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", length = 10)
    private FareMode transportMode;

    public static Trip from(UUID externalTransactionId,
                            UUID cardId,
                            UUID ticketId, UUID operatorId,
                            UUID tapInStationId, Instant tapInAt,
                            UUID tapOutStationId, Instant tapOutAt,
                            BigDecimal distanceKm, BigDecimal fareAmount,
                            FareMode transportMode,
                            TicketTypeUsed ticketTypeUsed) {
        Trip t                    = new Trip();
        t.id                      = UUID.randomUUID();
        t.externalTransactionId   = externalTransactionId;
        t.cardId                  = cardId;
        t.ticketId                = ticketId;
        t.operatorId              = operatorId;
        t.tapInStationId          = tapInStationId;
        t.tapInAt                 = tapInAt;
        t.tapOutStationId         = tapOutStationId;
        t.tapOutAt                = tapOutAt;
        t.distanceKm              = distanceKm;
        t.fareAmount              = fareAmount;
        t.transportMode           = transportMode;
        t.ticketTypeUsed          = ticketTypeUsed;
        return t;
    }

    public void correctFare(BigDecimal correctedFare) {
        if (correctedFare == null
                || correctedFare.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessRuleException(ErrorCode.INVALID_FARE_AMOUNT);
        this.fareAmount = correctedFare;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}