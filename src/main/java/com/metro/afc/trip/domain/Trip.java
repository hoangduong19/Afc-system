package com.metro.afc.trip.domain;

import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.trip.domain.enums.trip.PaymentMethod;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
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

    @Column(name = "tap_in_gate_id", nullable = false, length = 50)
    private String tapInGateId;

    @Column(name = "tap_in_at", nullable = false)
    private Instant tapInAt;

    @Column(name = "tap_out_station_id", columnDefinition = "uuid")
    private UUID tapOutStationId;

    @Column(name = "tap_out_gate_id", length = 50)
    private String tapOutGateId;

    @Column(name = "tap_out_at")
    private Instant tapOutAt;

    @Column(name = "distance_km", precision = 10, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "fare_amount", precision = 15, scale = 2)
    private BigDecimal fareAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type_used", length = 20)
    private TicketTypeUsed ticketTypeUsed;

    @Column(name = "ticket_id", columnDefinition = "uuid")
    private UUID ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "debt_amount", precision = 15, scale = 2)
    private BigDecimal debtAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Trip from(UUID externalTransactionId,
                            UUID cardId,
                            UUID ticketId, UUID operatorId,
                            UUID tapInStationId, String tapInGateId, Instant tapInAt,
                            UUID tapOutStationId, String tapOutGateId, Instant tapOutAt,
                            BigDecimal distanceKm, BigDecimal fareAmount,
                            PaymentMethod paymentMethod, TicketTypeUsed ticketTypeUsed,
                            TripStatus status, BigDecimal debtAmount) {
        Trip t                    = new Trip();
        t.id                      = UUID.randomUUID();
        t.externalTransactionId   = externalTransactionId;
        t.cardId                  = cardId;
        t.ticketId                = ticketId;
        t.operatorId              = operatorId;
        t.tapInStationId          = tapInStationId;
        t.tapInGateId             = tapInGateId;
        t.tapInAt                 = tapInAt;
        t.tapOutStationId         = tapOutStationId;
        t.tapOutGateId            = tapOutGateId;
        t.tapOutAt                = tapOutAt;
        t.distanceKm              = distanceKm;
        t.fareAmount              = fareAmount;
        t.paymentMethod           = paymentMethod;
        t.ticketTypeUsed          = ticketTypeUsed;
        t.status                  = status;
        t.debtAmount              = debtAmount;
        return t;
    }

    public void correctFare(BigDecimal correctedFare) {
        if (correctedFare == null
                || correctedFare.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessRuleException(
                    ErrorCode.INVALID_FARE_AMOUNT);
        this.fareAmount = correctedFare;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}
