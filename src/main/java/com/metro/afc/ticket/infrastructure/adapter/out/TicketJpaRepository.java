package com.metro.afc.ticket.infrastructure.adapter.out;

import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByCardIdAndStatus(UUID cardId, TicketStatus status);
    boolean existsByCardIdAndStatus(UUID cardId, TicketStatus status);
    List<Ticket> findByUserId(UUID userId);
    List<Ticket> findByUserIdAndStatus(UUID userId, TicketStatus status);
    Optional<Ticket> findByCardIdAndTypeAndStatus(
            UUID cardId, TicketType type, TicketStatus status);

    @Modifying
    @Query("UPDATE Ticket t SET t.status = 'EXPIRED' " +
            "WHERE t.status = 'ACTIVE' AND t.validTo < :today")
    int expireOverdueTickets(@Param("today") LocalDate today);
}