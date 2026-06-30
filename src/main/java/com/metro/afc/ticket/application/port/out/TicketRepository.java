package com.metro.afc.ticket.application.port.out;

import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    List<Ticket> saveAll(List<Ticket> tickets);
    Optional<Ticket> findById(UUID id);
    Optional<Ticket> findActiveTicketByCardId(UUID cardId);
    boolean existsActiveTicketByCardId(UUID cardId);
    int expireOverdueTickets(LocalDate today);
    List<Ticket> findByUserId(UUID userId);
    List<Ticket> findByUserIdAndStatus(UUID userId, TicketStatus status);
    Optional<Ticket> findActiveByCardIdAndType(UUID cardId, TicketType type);
    Page<Ticket> findAllWithFilters(TicketType type, TicketStatus status,
                                    LocalDate fromDate, LocalDate toDate, Pageable pageable);
    List<Ticket> findAllByIds(Collection<UUID> ids);
    List<Ticket> findActiveInPeriod(LocalDate from, LocalDate to);
}