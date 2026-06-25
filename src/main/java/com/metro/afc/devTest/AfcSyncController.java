package com.metro.afc.devTest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/afc")
@RequiredArgsConstructor
public class AfcSyncController {

    private final SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<Void> syncAll() {
        syncService.syncAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/cards")
    public ResponseEntity<Void> syncCards() {
        syncService.syncCards();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/tickets")
    public ResponseEntity<Void> syncTickets() {
        syncService.syncTickets();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/operators")
    public ResponseEntity<Void> syncOperators() {
        syncService.syncOperators();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync/blacklists")
    public ResponseEntity<Void> syncBlacklists() {
        syncService.syncBlacklists();
        return ResponseEntity.ok().build();
    }
}