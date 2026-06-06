package com.metro.afc.fare.application.port.in;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.FareMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FareRuleUseCase {
    FareRule create(String code, FareMode mode,
                    BigDecimal baseFare, BigDecimal ratePerKm,
                    BigDecimal minPrice, BigDecimal maxPrice,
                    LocalDate effectiveFrom, LocalDate effectiveTo,
                    UUID createdBy);

    FareRule update(UUID id, BigDecimal baseFare, BigDecimal ratePerKm,
                    BigDecimal minPrice, BigDecimal maxPrice,
                    LocalDate effectiveFrom, LocalDate effectiveTo,
                    UUID updatedBy);

    void disable(UUID id, UUID disabledBy);

    FareRule findById(UUID id);
    List<FareRule> findAllActive();
    List<FareRule> findAll();
}