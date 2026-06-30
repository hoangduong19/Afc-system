package com.metro.afc.trip.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExternalTransactionBatchRequest(
        @NotEmpty @Size(max = 10000)
        List<ExternalTransactionItemRequest> transactions
) {}