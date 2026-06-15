package com.metro.afc.operator.domain.model;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.operator.domain.model.enums.OperatorStatus;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operators")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperatorStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 10, nullable = false)
    private FareMode mode;

    // ── Factory method ───────────────────────────────────────────

    public static Operator create(String code, String name) {
        Operator operator = new Operator();
        operator.code   = code.trim().toUpperCase();
        operator.name   = name.trim();
        operator.status = OperatorStatus.ACTIVE;
        return operator;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public void update(String name) {
        this.name = name.trim();
    }

    public void deactivate() {
        if (this.status == OperatorStatus.INACTIVE) {
            throw new BusinessRuleException(
                    ErrorCode.VALIDATION_ERROR,
                    "Operator đã ở trạng thái INACTIVE"
            );
        }
        this.status = OperatorStatus.INACTIVE;
    }

    public void activate() {
        if (this.status == OperatorStatus.ACTIVE) {
            throw new BusinessRuleException(
                    ErrorCode.VALIDATION_ERROR,
                    "Operator đã ở trạng thái ACTIVE"
            );
        }
        this.status = OperatorStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == OperatorStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}