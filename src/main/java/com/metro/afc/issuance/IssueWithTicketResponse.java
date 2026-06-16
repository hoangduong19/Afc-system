package com.metro.afc.issuance;

import com.metro.afc.card.application.dto.card.CardResponse;
import com.metro.afc.ticket.application.dto.TicketResponse;

public record IssueWithTicketResponse(
        CardResponse card,
        TicketResponse ticket
) {}