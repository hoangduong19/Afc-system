package com.metro.afc.fare.infrastructure.adapter.in;

import com.metro.afc.fare.application.dto.fareRuleDiscount.CreateFareDiscountRequest;
import com.metro.afc.fare.application.dto.fareRuleDiscount.FareDiscountResponse;
import com.metro.afc.fare.application.dto.fareRuleDiscount.UpdateFareDiscountRequest;
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
@RequestMapping("/api/fare-discounts")
@RequiredArgsConstructor
public class FareDiscountController {

    private final FareDiscountFacade fareDiscountFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('DISCOUNT_CREATE')")
    public ResponseEntity<FareDiscountResponse> create(
            @Valid @RequestBody CreateFareDiscountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareDiscountFacade.create(request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DISCOUNT_UPDATE')")
    public ResponseEntity<FareDiscountResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFareDiscountRequest request) {
        return ResponseEntity.ok(
                fareDiscountFacade.update(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('DISCOUNT_UPDATE')")
    public ResponseEntity<Void> disable(@PathVariable UUID id) {
        fareDiscountFacade.disable(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<FareDiscountResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(fareDiscountFacade.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<List<FareDiscountResponse>> findAll() {
        return ResponseEntity.ok(fareDiscountFacade.findAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<List<FareDiscountResponse>> findAllActive() {
        return ResponseEntity.ok(fareDiscountFacade.findAllActive());
    }
}