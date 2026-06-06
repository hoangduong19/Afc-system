package com.metro.afc.fare.application;

import com.metro.afc.fare.application.port.in.FareRuleUseCase;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.FareMode;
import com.metro.afc.fare.domain.model.enums.FareStatus;
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
public class FareRuleService implements FareRuleUseCase {

    private final FareRuleRepository fareRuleRepository;

    @Override
    @Transactional
    public FareRule create(String code, FareMode mode,
                           BigDecimal baseFare, BigDecimal ratePerKm,
                           BigDecimal minPrice, BigDecimal maxPrice,
                           LocalDate effectiveFrom, LocalDate effectiveTo,
                           UUID createdBy) {
        if (fareRuleRepository.existsByCodeAndStatus(
                code.trim().toUpperCase(), FareStatus.ACTIVE)) {
            throw new ConflictException(ErrorCode.FARE_RULE_ALREADY_EXISTS);
        }
        return fareRuleRepository.save(
                FareRule.create(code, mode, baseFare, ratePerKm,
                        minPrice, maxPrice, effectiveFrom, effectiveTo, createdBy)
        );
    }

    @Override
    @Transactional
    public FareRule update(UUID id, BigDecimal baseFare, BigDecimal ratePerKm,
                           BigDecimal minPrice, BigDecimal maxPrice,
                           LocalDate effectiveFrom, LocalDate effectiveTo,
                           UUID updatedBy) {
        FareRule current = findOrThrow(id);
        if (!current.isActive()) {
            throw new BusinessRuleException(ErrorCode.FARE_RULE_INACTIVE);
        }
        current.closeVersion(effectiveFrom);
        fareRuleRepository.save(current);
        return fareRuleRepository.save(
                current.newVersion(baseFare, ratePerKm, minPrice, maxPrice,
                        effectiveFrom, effectiveTo, updatedBy)
        );
    }

    @Override
    @Transactional
    public void disable(UUID id, UUID disabledBy) {
        FareRule fareRule = findOrThrow(id);
        fareRule.disable(disabledBy);
        fareRuleRepository.save(fareRule);
    }

    @Override
    public FareRule findById(UUID id) {
        return findOrThrow(id);
    }

    @Override
    public List<FareRule> findAllActive() {
        return fareRuleRepository.findAllActive();
    }

    @Override
    public List<FareRule> findAll() {
        return fareRuleRepository.findAll();
    }

    private FareRule findOrThrow(UUID id) {
        return fareRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FARE_RULE_NOT_FOUND));
    }
}