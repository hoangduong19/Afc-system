package com.metro.afc.devTest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/afc")
@RequiredArgsConstructor
public class AfcSyncController {

    private final SyncService syncService;

    @PostMapping("/sync")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<Void> syncAll() {
        syncService.syncAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/cards")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<Void> syncCards() {
        syncService.syncCards();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/tickets")
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public ResponseEntity<Void> syncTickets() {
        syncService.syncTickets();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/operators")
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<Void> syncOperators() {
        syncService.syncOperators();
        return ResponseEntity.ok().build();
    }
}