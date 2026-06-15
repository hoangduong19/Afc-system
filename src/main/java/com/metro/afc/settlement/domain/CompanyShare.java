package com.metro.afc.settlement.domain;

import com.metro.afc.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "company_shares")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyShare {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "settlement_id", nullable = false,
            columnDefinition = "uuid")
    private UUID settlementId;

    @Column(name = "operator_id", nullable = false,
            columnDefinition = "uuid")
    private UUID operatorId;

    @Column(name = "total_km", nullable = false,
            precision = 15, scale = 3)
    private BigDecimal totalKm;

    @Column(name = "total_trips", nullable = false)
    private Integer totalTrips;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "expected_revenue",
                    nullable = false, precision = 15, scale = 2))
    private Money expectedRevenue;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "share_amount",
                    nullable = false, precision = 15, scale = 2))
    private Money shareAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "direct_share", precision = 15, scale = 2))
    })
    private Money directShare;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "proportional_share", precision = 15, scale = 2))
    })
    private Money proportionalShare;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "rounding_adjustment",
                    precision = 15, scale = 2))
    private Money roundingAdjustment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static CompanyShare of(UUID settlementId, UUID operatorId,
                                  BigDecimal totalKm, int totalTrips,
                                  Money expectedRevenue,
                                  Money shareAmount, Money directShare,
                                  Money proportionalShare,
                                  Money roundingAdjustment) {
        CompanyShare s       = new CompanyShare();
        s.id                 = UUID.randomUUID();
        s.settlementId       = settlementId;
        s.operatorId         = operatorId;
        s.totalKm            = totalKm;
        s.totalTrips         = totalTrips;
        s.expectedRevenue    = expectedRevenue;
        s.shareAmount        = shareAmount;
        s.directShare        = directShare;
        s.proportionalShare    = proportionalShare;
        s.roundingAdjustment = roundingAdjustment;
        return s;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}