package com.metro.afc.fare.infrastructure.adapter.in;

import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalFareRequest;
import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalFareResponse;
import com.metro.afc.fare.application.dto.fareRule.calculation.SingleTripFareRequest;
import com.metro.afc.fare.application.dto.fareRule.calculation.SingleTripFareResponse;
import com.metro.afc.fare.application.port.in.FareCalculationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fare-calculation")
@RequiredArgsConstructor
public class FareCalculationController {

    private final FareCalculationUseCase fareCalculationUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('FARE_READ')")
    public ResponseEntity<SingleTripFareResponse> calculate(
            @Valid @RequestBody SingleTripFareRequest request) {
        return ResponseEntity.ok(fareCalculationUseCase.calculate(
                request.fromStationId(),
                request.toStationId(),
                request.mode(),
                request.passengerType()
        ));
    }

    @PostMapping("/multimodal")
    @PreAuthorize("hasAuthority('FARE_READ')")
    public ResponseEntity<MultimodalFareResponse> calculateMultimodal(
            @Valid @RequestBody MultimodalFareRequest request) {
        return ResponseEntity.ok(fareCalculationUseCase.calculateMultimodal(
                request.legs(), request.passengerType()
        ));
    }
}