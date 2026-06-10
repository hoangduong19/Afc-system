package com.metro.afc.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReconciliationLog {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "settlement_id", nullable = false,
            columnDefinition = "uuid")
    private UUID settlementId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "discrepancy_amount", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal discrepancyAmount;

    @Column(name = "trip_count", nullable = false)
    private Integer tripCount;

    @Column
    private String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detail;

    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt;

    public static ReconciliationLog of(UUID settlementId, String category,
                                       BigDecimal discrepancyAmount,
                                       int tripCount, String note,
                                       Map<String, Object> detail) {
        ReconciliationLog r = new ReconciliationLog();
        r.id                = UUID.randomUUID();
        r.settlementId      = settlementId;
        r.category          = category;
        r.discrepancyAmount = discrepancyAmount;
        r.tripCount         = tripCount;
        r.note              = note;
        r.detail            = detail;
        return r;
    }

    @PrePersist
    protected void onCreate() { loggedAt = Instant.now(); }
}