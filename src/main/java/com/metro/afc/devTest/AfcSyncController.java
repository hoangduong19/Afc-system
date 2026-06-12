package com.metro.afc.devTest;

import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.infrastructure.adapter.out.persistence.card.CardJpaRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.operator.infrastructure.adapter.out.OperatorJpaRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.infrastructure.adapter.out.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/afc")
@RequiredArgsConstructor
public class AfcSyncController {

    private final CardJpaRepository cardJpaRepository;
    private final TicketJpaRepository ticketJpaRepository;
    private final OperatorJpaRepository operatorJpaRepository;

    @GetMapping("/sync/cards")
    public ResponseEntity<List<Card>> getAllCards() {
        return ResponseEntity.ok(cardJpaRepository.findAll());
    }

    @GetMapping("/sync/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketJpaRepository.findAll());
    }

    @GetMapping("/sync/operators")
    public ResponseEntity<List<Operator>> getAllOperators() {
        return ResponseEntity.ok(operatorJpaRepository.findAll());
    }
}