package com.metro.afc.passenger.controller;


import com.metro.afc.passenger.FareCatalogService;
import com.metro.afc.passenger.dto.DiscountResponse;
import com.metro.afc.passenger.dto.FarePriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/passenger/fare")
@RequiredArgsConstructor
public class FareCatalogController {

    private final FareCatalogService fareCatalogService;

    @GetMapping("/prices")
    public ResponseEntity<List<FarePriceResponse>> getPrices() {
        return ResponseEntity.ok(fareCatalogService.getFarePrices());
    }

    @GetMapping("/discounts")
    public ResponseEntity<List<DiscountResponse>> getDiscounts() {
        return ResponseEntity.ok(fareCatalogService.getActiveDiscounts());
    }
}