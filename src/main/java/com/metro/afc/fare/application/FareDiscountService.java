package com.metro.afc.fare.application;

import com.metro.afc.fare.application.port.in.FareDiscountUseCase;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountStatus;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FareDiscountService implements FareDiscountUseCase {

    private final FareDiscountRepository fareDiscountRepository;

    @Override
    @Transactional
    public FareDiscount create(PassengerType passengerType, DiscountType discountType,
                               BigDecimal value, LocalDate effectiveFrom,
                               LocalDate effectiveTo, UUID createdBy) {
        if (fareDiscountRepository.existsByPassengerTypeAndStatus(
                passengerType, DiscountStatus.ACTIVE))
            throw new ConflictException(ErrorCode.FARE_DISCOUNT_ALREADY_EXISTS);

        return fareDiscountRepository.save(
                FareDiscount.create(passengerType, discountType,
                        value, effectiveFrom, effectiveTo, createdBy)
        );
    }

    @Override
    @Transactional
    public FareDiscount update(UUID id, DiscountType discountType, BigDecimal value,
                               LocalDate effectiveFrom, LocalDate effectiveTo,
                               UUID updatedBy) {
        FareDiscount current = findOrThrow(id);
        if (!current.isActive())
            throw new BusinessRuleException(ErrorCode.FARE_DISCOUNT_INACTIVE,
                    "Cannot update an inactive fare discount");

        current.closeVersion(effectiveFrom);
        fareDiscountRepository.save(current);

        return fareDiscountRepository.save(
                current.newVersion(discountType, value, effectiveFrom, effectiveTo, updatedBy)
        );
    }

    @Override
    @Transactional
    public void disable(UUID id, UUID disabledBy) {
        FareDiscount discount = findOrThrow(id);
        discount.disable(disabledBy);
        fareDiscountRepository.save(discount);
    }

    @Override
    public FareDiscount findById(UUID id) { return findOrThrow(id); }

    @Override
    public List<FareDiscount> findAll() {
        return fareDiscountRepository.findAll();
    }

    @Override
    public List<FareDiscount> findAllActive() {
        return fareDiscountRepository.findAllActive();
    }

    private FareDiscount findOrThrow(UUID id) {
        return fareDiscountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FARE_DISCOUNT_NOT_FOUND));
    }
}