package com.metro.afc.route.infrastructure.adapter.in;

import com.metro.afc.route.application.dtos.CreateRouteRequest;
import com.metro.afc.route.application.dtos.RouteResponse;
import com.metro.afc.route.application.dtos.UpdateRouteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteFacade routeFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('OPERATOR_CREATE')")
    public ResponseEntity<RouteResponse> create(
            @Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeFacade.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<RouteResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request) {
        return ResponseEntity.ok(routeFacade.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        routeFacade.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<RouteResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(routeFacade.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<List<RouteResponse>> findAll() {
        return ResponseEntity.ok(routeFacade.findAll());
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<List<RouteResponse>> findByOperatorId(
            @PathVariable UUID operatorId) {
        return ResponseEntity.ok(routeFacade.findByOperatorId(operatorId));
    }
}