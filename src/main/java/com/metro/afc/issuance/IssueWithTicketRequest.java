package com.metro.afc.issuance;

import com.metro.afc.card.application.dto.card.CreateCardRequest;
import com.metro.afc.ticket.application.dto.CreatePassRequest;
import jakarta.validation.constraints.NotNull;

public record IssueWithTicketRequest(
        @NotNull CreateCardRequest card,
        CreatePassRequest ticket
) {}