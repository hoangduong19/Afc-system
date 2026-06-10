package com.metro.afc.settlement.infrastructure.adapter.in;

import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import com.metro.afc.settlement.application.dto.settlement.ReconciliationLogResponse;
import com.metro.afc.settlement.application.dto.settlement.RunSettlementRequest;
import com.metro.afc.settlement.application.dto.settlement.SettlementResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementFacade facade;

    @PostMapping("/run")
    @PreAuthorize("hasAuthority('SETTLEMENT_RUN')")
    public ResponseEntity<SettlementResponse> run(
            @Valid @RequestBody RunSettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.run(request,
                        SecurityUtils.getCurrentUserId()));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('SETTLEMENT_CONFIRM')")
    public ResponseEntity<SettlementResponse> confirm(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                facade.confirm(id, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTLEMENT_READ')")
    public ResponseEntity<SettlementResponse> findById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(facade.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SETTLEMENT_READ')")
    public ResponseEntity<List<SettlementResponse>> findAll() {
        return ResponseEntity.ok(facade.findAll());
    }

    @GetMapping("/{id}/reconciliation-logs")
    @PreAuthorize("hasAuthority('SETTLEMENT_READ')")
    public ResponseEntity<List<ReconciliationLogResponse>> getLogs(
            @PathVariable UUID id) {
        return ResponseEntity.ok(facade.findLogs(id));
    }
}