package com.metro.afc.ticket.application.port.out;

import com.metro.afc.ticket.domain.Ticket;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(UUID id);
    Optional<Ticket> findActiveTicketByCardId(UUID cardId);
    boolean existsActiveTicketByCardId(UUID cardId);
    int expireOverdueTickets(LocalDate today);
}