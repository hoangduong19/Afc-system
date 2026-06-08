package com.metro.afc.fare.application.port.out;

import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountStatus;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FareDiscountRepository {
    Optional<FareDiscount> findById(UUID id);
    List<FareDiscount> findAll();
    List<FareDiscount> findAllActive();
    boolean existsByPassengerTypeAndStatus(PassengerType type, DiscountStatus status);
    FareDiscount save(FareDiscount discount);
}