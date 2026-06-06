package com.metro.afc.fare.application.dto;

import com.metro.afc.fare.domain.model.enums.FareMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFareRuleRequest(
        @NotBlank(message = "Code không được để trống")
        @Size(max = 50, message = "Code tối đa 50 ký tự")
        String code,

        @NotNull(message = "Mode không được để trống")
        FareMode mode,

        @NotNull(message = "Base fare không được để trống")
        @DecimalMin(value = "0.0", message = "Base fare phải >= 0")
        BigDecimal baseFare,

        @NotNull(message = "Rate per km không được để trống")
        @DecimalMin(value = "0.0", message = "Rate per km phải >= 0")
        BigDecimal ratePerKm,

        @NotNull(message = "Min price không được để trống")
        @DecimalMin(value = "0.0", message = "Min price phải >= 0")
        BigDecimal minPrice,

        @NotNull(message = "Max price không được để trống")
        @DecimalMin(value = "0.0", message = "Max price phải >= 0")
        BigDecimal maxPrice,

        @NotNull(message = "Effective from không được để trống")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) {
    public CreateFareRuleRequest {
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Min price không được lớn hơn max price");
        }
        if (effectiveTo != null && effectiveFrom != null
                && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective to không được trước effective from");
        }
    }
}