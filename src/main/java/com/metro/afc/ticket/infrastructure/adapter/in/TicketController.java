package com.metro.afc.ticket.infrastructure.adapter.in;

import com.metro.afc.ticket.application.dto.CreatePassRequest;
import com.metro.afc.ticket.application.dto.CreateSingleTripTicketRequest;
import com.metro.afc.ticket.application.dto.TicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketFacade ticketFacade;

    @PostMapping("/single-trip")
    @PreAuthorize("hasAuthority('TICKET_PURCHASE')")
    public ResponseEntity<TicketResponse> createSingleTrip(
            @Valid @RequestBody CreateSingleTripTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketFacade.createSingleTrip(
                        request
                ));
    }

    @PostMapping("/pass")
    @PreAuthorize("hasAuthority('TICKET_PURCHASE')")
    public ResponseEntity<TicketResponse> createPass(
            @Valid @RequestBody CreatePassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketFacade.createMonthlyPass(
                        request
                ));
    }
}