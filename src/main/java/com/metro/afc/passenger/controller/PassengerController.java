package com.metro.afc.passenger.controller;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import com.metro.afc.passenger.dto.PassengerCardResponse;
import com.metro.afc.passenger.dto.PassengerTicketResponse;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.shared.infrastructure.exception.UnauthorizedException;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PassengerController {

    private final TicketRepository ticketRepository;
    private final CardRepository cardRepository;

    // ── GET /me/tickets ─────────────────────────────────────────
    @GetMapping("/api/me/tickets")
    @PreAuthorize("hasAuthority('TICKET_READ_OWN')")
    public ResponseEntity<List<PassengerTicketResponse>> getMyTickets(
            @RequestParam(required = false) TicketStatus status) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<Ticket> tickets = status != null
                ? ticketRepository.findByUserIdAndStatus(userId, status)
                : ticketRepository.findByUserId(userId);

        return ResponseEntity.ok(
                tickets.stream()
                        .map(PassengerTicketResponse::from).toList()
        );
    }

    // ── GET /tickets/{id} ───────────────────────────────────────
    @GetMapping("/api/tickets/{id}")
    @PreAuthorize("hasAuthority('TICKET_READ_OWN')")
    public ResponseEntity<PassengerTicketResponse> getTicket(
            @PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TICKET_NOT_FOUND));

        if (!ticket.getUserId().equals(userId))
            throw new UnauthorizedException(ErrorCode.FORBIDDEN);


        return ResponseEntity.ok(PassengerTicketResponse.from(ticket));
    }

    // ── GET /me/cards ───────────────────────────────────────────
    @GetMapping("/api/me/cards")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<List<PassengerCardResponse>> getMyCards() {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<Card> cards = cardRepository.findByLinkedUserId(userId);

        return ResponseEntity.ok(
                cards.stream()
                        .map(PassengerCardResponse::from).toList()
        );
    }
}