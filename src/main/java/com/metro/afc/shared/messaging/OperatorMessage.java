package com.metro.afc.shared.messaging;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.operator.domain.model.enums.OperatorStatus;

import java.util.UUID;

public record OperatorMessage(
        UUID operatorId,
        String code,
        String name,
        OperatorStatus status,
        FareMode mode
) {}