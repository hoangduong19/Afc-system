package com.metro.afc.ticket.infrastructure.adapter.out;

import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByCardIdAndStatus(UUID cardId, TicketStatus status);
    boolean existsByCardIdAndStatus(UUID cardId, TicketStatus status);
}