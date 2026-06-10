package com.metro.afc.ticket.domain.events;

import com.metro.afc.ticket.domain.Ticket;

public record TicketLinkedToCardEvent(Ticket ticket) {}