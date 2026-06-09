package com.metro.afc.fare.infrastructure.adapter.out.fareDiscount;

import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountStatus;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FareDiscountJpaRepository
        extends JpaRepository<FareDiscount, UUID> {
    List<FareDiscount> findAllByStatus(DiscountStatus status);
    boolean existsByPassengerTypeAndStatus(
            PassengerType type, DiscountStatus status);
    Optional<FareDiscount> findByPassengerTypeAndStatus(
            PassengerType type, DiscountStatus status);
}