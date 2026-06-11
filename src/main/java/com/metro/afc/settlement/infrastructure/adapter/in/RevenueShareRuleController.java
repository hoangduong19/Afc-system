package com.metro.afc.settlement.infrastructure.adapter.in;

import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import com.metro.afc.settlement.application.dto.revenueShareRule.CreateRevenueShareRuleRequest;
import com.metro.afc.settlement.application.dto.revenueShareRule.RevenueShareRuleResponse;
import com.metro.afc.settlement.application.dto.revenueShareRule.UpdateRevenueShareRuleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/revenue-share-rules")
@RequiredArgsConstructor
public class RevenueShareRuleController {

    private final RevenueShareRuleFacade facade;

    @PostMapping
    @PreAuthorize("hasAuthority('SETTLEMENT_RUN')")
    public ResponseEntity<RevenueShareRuleResponse> create(
            @Valid @RequestBody CreateRevenueShareRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.create(request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAuthority('SETTLEMENT_RUN')")
    public ResponseEntity<RevenueShareRuleResponse> update(
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateRevenueShareRuleRequest request) {
        return ResponseEntity.ok(
                facade.update(ruleId, request, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAuthority('SETTLEMENT_CONFIRM')")
    public ResponseEntity<RevenueShareRuleResponse> disable(
            @PathVariable UUID ruleId) {
        return ResponseEntity.ok(
                facade.disable(ruleId, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasAuthority('SETTLEMENT_READ')")
    public ResponseEntity<RevenueShareRuleResponse> getActive(
            @PathVariable UUID operatorId) {
        return ResponseEntity.ok(facade.findActiveByOperatorId(operatorId));
    }

    @GetMapping("/operator/{operatorId}/history")
    @PreAuthorize("hasAuthority('SETTLEMENT_READ')")
    public ResponseEntity<List<RevenueShareRuleResponse>> getHistory(
            @PathVariable UUID operatorId) {
        return ResponseEntity.ok(facade.findAllByOperatorId(operatorId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SETTLEMENT_READ')")
    public ResponseEntity<List<RevenueShareRuleResponse>> findAll() {
        return ResponseEntity.ok(facade.findAll());
    }
}