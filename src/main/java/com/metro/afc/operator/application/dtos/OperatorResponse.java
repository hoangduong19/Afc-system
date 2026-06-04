package com.metro.afc.operator.application.dtos;

import com.metro.afc.operator.domain.model.Operator;

import java.time.Instant;
import java.util.UUID;

public record OperatorResponse(
        UUID id,
        String code,
        String name,
        String status,
        Instant createdAt
) {
    public static OperatorResponse from(Operator operator) {
        return new OperatorResponse(
                operator.getId(),
                operator.getCode(),
                operator.getName(),
                operator.getStatus().name(),
                operator.getCreatedAt()
        );
    }
}