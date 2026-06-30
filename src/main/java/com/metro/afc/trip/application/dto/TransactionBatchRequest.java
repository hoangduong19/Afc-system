package com.metro.afc.trip.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TransactionBatchRequest(
        @NotNull @Size(min = 1, max = 10000)
        List<TransactionItemRequest> transactions
) {}