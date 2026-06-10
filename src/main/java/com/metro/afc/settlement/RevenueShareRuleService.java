package com.metro.afc.settlement;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.settlement.application.port.in.RevenueShareRuleUseCase;
import com.metro.afc.settlement.application.port.out.RevenueShareRuleRepository;
import com.metro.afc.settlement.domain.RevenueShareRule;
import com.metro.afc.settlement.domain.enums.revenueShare.ShareModel;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevenueShareRuleService implements RevenueShareRuleUseCase {

    private final RevenueShareRuleRepository ruleRepository;
    private final OperatorRepository operatorRepository;

    @Override
    @Transactional
    public RevenueShareRule create(UUID operatorId, ShareModel shareModel,
                                   Map<String, Object> params,
                                   BigDecimal sharePercentage,
                                   LocalDate effectiveFrom, LocalDate effectiveTo,
                                   UUID createdBy) {
        operatorRepository.findById(operatorId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.OPERATOR_NOT_FOUND));

        if (ruleRepository.existsActiveByOperatorId(operatorId))
            throw new BusinessRuleException(
                    ErrorCode.REVENUE_SHARE_RULE_ALREADY_EXISTS,
                    "Operator already has an active rule. Use update to create new version."
            );

        return ruleRepository.save(
                RevenueShareRule.create(operatorId, shareModel, params,
                        sharePercentage, effectiveFrom, effectiveTo, createdBy)
        );
    }

    @Override
    @Transactional
    public RevenueShareRule update(UUID ruleId, ShareModel shareModel,
                                   Map<String, Object> params,
                                   BigDecimal sharePercentage,
                                   LocalDate effectiveFrom, LocalDate effectiveTo,
                                   UUID updatedBy) {
        RevenueShareRule current = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.REVENUE_SHARE_RULE_NOT_FOUND));

        // Disable version cũ + tạo version mới
        RevenueShareRule newVersion = current.createNewVersion(
                shareModel, params, sharePercentage,
                effectiveFrom, effectiveTo, updatedBy
        );

        ruleRepository.save(current);       // save version cũ (INACTIVE)
        return ruleRepository.save(newVersion); // save version mới (ACTIVE)
    }

    @Override
    @Transactional
    public RevenueShareRule disable(UUID ruleId, UUID disabledBy) {
        RevenueShareRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.REVENUE_SHARE_RULE_NOT_FOUND));
        rule.disable(disabledBy);
        return ruleRepository.save(rule);
    }

    @Override
    public Optional<RevenueShareRule> findActiveByOperatorId(UUID operatorId) {
        return ruleRepository.findActiveByOperatorId(operatorId);
    }

    @Override
    public List<RevenueShareRule> findAllByOperatorId(UUID operatorId) {
        return ruleRepository.findAllByOperatorId(operatorId);
    }
}