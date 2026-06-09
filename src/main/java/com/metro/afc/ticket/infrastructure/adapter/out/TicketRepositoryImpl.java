package com.metro.afc.ticket.infrastructure.adapter.out;

import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepository {

    private final TicketJpaRepository jpa;

    @Override
    public Ticket save(Ticket ticket) { return jpa.save(ticket); }

    @Override
    public Optional<Ticket> findById(UUID id) { return jpa.findById(id); }

    @Override
    public Optional<Ticket> findActiveTicketByCardId(UUID cardId) {
        return jpa.findByCardIdAndStatus(cardId, TicketStatus.ACTIVE);
    }

    @Override
    public boolean existsActiveTicketByCardId(UUID cardId) {
        return jpa.existsByCardIdAndStatus(cardId, TicketStatus.ACTIVE);
    }

    @Override
    public int expireOverdueTickets(LocalDate today) {
        return jpa.expireOverdueTickets(today);
    }
}