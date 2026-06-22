package com.metro.afc.operator.domain.model;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.operator.domain.event.OperatorCreatedEvent;
import com.metro.afc.operator.domain.event.OperatorUpdatedEvent;
import com.metro.afc.operator.domain.model.enums.OperatorStatus;
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
@Table(name = "operators")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Operator extends AbstractAggregateRoot<Operator> {

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

    public static Operator create(String code, String name, FareMode mode) {
        Operator operator = new Operator();
        operator.code   = code.trim().toUpperCase();
        operator.name   = name.trim();
        operator.status = OperatorStatus.ACTIVE;
        operator.mode   = mode;
        operator.registerEvent(new OperatorCreatedEvent(operator));
        return operator;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public void update(String name) {
        this.name = name.trim();
        registerEvent(new OperatorUpdatedEvent(this));
    }

    public void deactivate() {
        if (this.status == OperatorStatus.INACTIVE) {
            throw new BusinessRuleException(
                    ErrorCode.VALIDATION_ERROR,
                    "Operator đã ở trạng thái INACTIVE"
            );
        }
        this.status = OperatorStatus.INACTIVE;
        registerEvent(new OperatorUpdatedEvent(this));
    }

    public void activate() {
        if (this.status == OperatorStatus.ACTIVE) {
            throw new BusinessRuleException(
                    ErrorCode.VALIDATION_ERROR,
                    "Operator đã ở trạng thái ACTIVE"
            );
        }
        this.status = OperatorStatus.ACTIVE;
        registerEvent(new OperatorUpdatedEvent(this));
    }

    public boolean isActive() {
        return this.status == OperatorStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}