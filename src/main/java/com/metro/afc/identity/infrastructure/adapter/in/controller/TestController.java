package com.metro.afc.identity.infrastructure.adapter.in.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ACC_ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Bạn là ACC_ADMIN");
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('STATION_STAFF')")
    public ResponseEntity<String> staffOnly() {
        return ResponseEntity.ok("Bạn là STATION_STAFF");
    }

    @GetMapping("/card-issue")
    @PreAuthorize("hasAuthority('CARD_ISSUE')")
    public ResponseEntity<String> cardIssue() {
        return ResponseEntity.ok("Bạn có quyền CARD_ISSUE");
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok("User: " + authentication.getName()
                + " | Authorities: " + authentication.getAuthorities());
    }
}