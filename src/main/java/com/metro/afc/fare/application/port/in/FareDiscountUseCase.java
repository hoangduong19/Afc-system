package com.metro.afc.fare.application.port.in;

import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FareDiscountUseCase {
    FareDiscount create(PassengerType passengerType, DiscountType discountType,
                        BigDecimal value, LocalDate effectiveFrom,
                        LocalDate effectiveTo, UUID createdBy);
    FareDiscount update(UUID id, DiscountType discountType, BigDecimal value,
                        LocalDate effectiveFrom, LocalDate effectiveTo,
                        UUID updatedBy);
    void disable(UUID id, UUID disabledBy);
    FareDiscount findById(UUID id);
    List<FareDiscount> findAll();
    List<FareDiscount> findAllActive();
}