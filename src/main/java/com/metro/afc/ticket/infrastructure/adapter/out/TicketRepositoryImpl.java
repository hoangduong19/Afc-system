package com.metro.afc.ticket.infrastructure.adapter.out;

import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
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
    public List<Ticket> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public List<Ticket> findByUserIdAndStatus(UUID userId, TicketStatus status) {
        return jpa.findByUserIdAndStatus(userId, status);
    }

    @Override
    public Optional<Ticket> findActiveByCardIdAndType(UUID cardId, TicketType type) {
        return jpa.findByCardIdAndTypeAndStatus(cardId, type, TicketStatus.ACTIVE);
    }

    @Override
    public int expireOverdueTickets(LocalDate today) {
        return jpa.expireOverdueTickets(today);
    }
}