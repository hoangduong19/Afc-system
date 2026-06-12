package com.metro.afc.operator.infrastructure.adapter.in;

import com.metro.afc.operator.application.dtos.CreateOperatorRequest;
import com.metro.afc.operator.application.dtos.OperatorResponse;
import com.metro.afc.operator.application.dtos.UpdateOperatorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorFacade operatorFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('OPERATOR_CREATE')")
    public ResponseEntity<OperatorResponse> create(
            @Valid @RequestBody CreateOperatorRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(operatorFacade.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<OperatorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOperatorRequest request) {
        return ResponseEntity.ok(operatorFacade.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        operatorFacade.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        operatorFacade.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<OperatorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(operatorFacade.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<List<OperatorResponse>> findAll() {
        return ResponseEntity.ok(operatorFacade.findAll());
    }
}