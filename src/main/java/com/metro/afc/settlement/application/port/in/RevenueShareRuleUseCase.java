package com.metro.afc.settlement.application.port.in;

import com.metro.afc.settlement.domain.RevenueShareRule;
import com.metro.afc.settlement.domain.enums.revenueShare.ShareModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface RevenueShareRuleUseCase {
    RevenueShareRule create(UUID operatorId, ShareModel shareModel,
                            Map<String, Object> params, BigDecimal sharePercentage,
                            LocalDate effectiveFrom, LocalDate effectiveTo,
                            UUID createdBy);

    RevenueShareRule update(UUID ruleId, ShareModel shareModel,
                            Map<String, Object> params, BigDecimal sharePercentage,
                            LocalDate effectiveFrom, LocalDate effectiveTo,
                            UUID updatedBy);

    RevenueShareRule disable(UUID ruleId, UUID disabledBy);

    Optional<RevenueShareRule> findActiveByOperatorId(UUID operatorId);
    List<RevenueShareRule> findAllByOperatorId(UUID operatorId);
    List<RevenueShareRule> findAll();
}