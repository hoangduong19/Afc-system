package com.metro.afc.passenger.controller;

import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.passenger.dto.PassengerCardResponse;
import com.metro.afc.passenger.dto.PassengerTicketResponse;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.UnauthorizedException;
import com.metro.afc.ticket.application.port.in.TicketUseCase;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PassengerController {

    private final TicketUseCase ticketUseCase;
    private final CardUseCase cardUseCase;

    @GetMapping("/api/passengers/{userId}/tickets")
    public ResponseEntity<List<PassengerTicketResponse>> getMyTickets(
            @PathVariable UUID userId,
            @RequestParam(required = false) TicketStatus status) {
        return ResponseEntity.ok(
                ticketUseCase.findByUserId(userId, status).stream()
                        .map(PassengerTicketResponse::from).toList()
        );
    }

    @GetMapping("/api/passengers/{userId}/tickets/{id}")
    public ResponseEntity<PassengerTicketResponse> getTicket(
            @PathVariable UUID userId,
            @PathVariable UUID id) {
        Ticket ticket = ticketUseCase.findById(id);
        if (!ticket.getUserId().equals(userId))
            throw new UnauthorizedException(ErrorCode.FORBIDDEN);
        return ResponseEntity.ok(PassengerTicketResponse.from(ticket));
    }

    @GetMapping("/api/passengers/{userId}/cards")
    public ResponseEntity<List<PassengerCardResponse>> getMyCards(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(
                cardUseCase.findByLinkedUserId(userId).stream()
                        .map(PassengerCardResponse::from).toList()
        );
    }
}