package com.metro.afc.ticket.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkTicketRequest(
        @NotNull(message = "Ticket ID is required")
        UUID ticketId
) {}