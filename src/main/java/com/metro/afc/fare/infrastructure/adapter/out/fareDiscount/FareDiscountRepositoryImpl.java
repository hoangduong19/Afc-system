package com.metro.afc.fare.infrastructure.adapter.out.fareDiscount;

import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountStatus;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FareDiscountRepositoryImpl implements FareDiscountRepository {

    private final FareDiscountJpaRepository jpa;

    @Override
    public Optional<FareDiscount> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<FareDiscount> findAll() { return jpa.findAll(); }

    @Override
    public List<FareDiscount> findAllActive() {
        return jpa.findAllByStatus(DiscountStatus.ACTIVE);
    }

    @Override
    public boolean existsByPassengerTypeAndStatus(
            PassengerType type, DiscountStatus status) {
        return jpa.existsByPassengerTypeAndStatus(type, status);
    }

    @Override
    public FareDiscount save(FareDiscount discount) {
        return jpa.save(discount);
    }
}