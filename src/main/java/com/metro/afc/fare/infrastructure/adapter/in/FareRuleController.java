package com.metro.afc.fare.infrastructure.adapter.in;

import com.metro.afc.fare.application.dto.fareRule.CreateFareRuleRequest;
import com.metro.afc.fare.application.dto.fareRule.DisableFareRuleRequest;
import com.metro.afc.fare.application.dto.fareRule.FareRuleResponse;
import com.metro.afc.fare.application.dto.fareRule.UpdateFareRuleRequest;
import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fare-rules")
@RequiredArgsConstructor
public class FareRuleController {

    private final FareRuleFacade fareRuleFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('FARE_CREATE')")
    public ResponseEntity<FareRuleResponse> create(
            @Valid @RequestBody CreateFareRuleRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fareRuleFacade.create(request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FARE_UPDATE')")
    public ResponseEntity<FareRuleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFareRuleRequest request) {
        return ResponseEntity.ok(
                fareRuleFacade.update(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('FARE_DISABLE')")
    public ResponseEntity<Void> disable(
            @PathVariable UUID id,
            @Valid @RequestBody DisableFareRuleRequest request) {
        fareRuleFacade.disable(id, request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FARE_READ')")
    public ResponseEntity<FareRuleResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(fareRuleFacade.findById(id));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('FARE_READ')")
    public ResponseEntity<List<FareRuleResponse>> findAllActive() {
        return ResponseEntity.ok(fareRuleFacade.findAllActive());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FARE_READ')")
    public ResponseEntity<List<FareRuleResponse>> findAll() {
        return ResponseEntity.ok(fareRuleFacade.findAll());
    }
}