package com.metro.afc.ticket.domain.events;

import java.util.UUID;

public record TicketUnlinkedFromCardEvent(UUID ticketId, UUID cardId) {}