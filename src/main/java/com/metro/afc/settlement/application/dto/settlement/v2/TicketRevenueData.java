package com.metro.afc.settlement.application.dto.settlement.v2;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.enums.PassScope;

import java.util.List;
import java.util.UUID;

public record TicketRevenueData(
        UUID ticketId,
        Money ticketPrice,
        FareMode mode,     // METRO | BUS | ANY
        PassScope scope,   // SINGLE_ROUTE | MULTI_ROUTE | null
        List<TripContribution> contributions
) {}