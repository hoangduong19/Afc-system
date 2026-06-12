package com.metro.afc.devTest.message;

import com.metro.afc.operator.domain.model.Operator;

import java.time.Instant;
import java.util.UUID;

public record OperatorSyncMessage(
        UUID id,
        String code,
        String name,
        String status,
        Instant createdAt
) {
    public static OperatorSyncMessage from(Operator operator) {
        return new OperatorSyncMessage(
                operator.getId(), operator.getCode(),
                operator.getName(), operator.getStatus().name(),
                operator.getCreatedAt()
        );
    }
}