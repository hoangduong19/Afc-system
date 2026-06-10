package com.metro.afc.settlement.infrastructure.adapter.in;

import com.metro.afc.settlement.application.dto.CreateRevenueShareRuleRequest;
import com.metro.afc.settlement.application.dto.RevenueShareRuleResponse;
import com.metro.afc.settlement.application.dto.UpdateRevenueShareRuleRequest;
import com.metro.afc.settlement.application.port.in.RevenueShareRuleUseCase;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevenueShareRuleFacade {

    private final RevenueShareRuleUseCase useCase;

    public RevenueShareRuleResponse create(CreateRevenueShareRuleRequest req,
                                           UUID createdBy) {
        return RevenueShareRuleResponse.from(
                useCase.create(req.operatorId(), req.shareModel(), req.params(),
                        req.sharePercentage(), req.effectiveFrom(), req.effectiveTo(),
                        createdBy)
        );
    }

    public RevenueShareRuleResponse update(UUID ruleId,
                                           UpdateRevenueShareRuleRequest req,
                                           UUID updatedBy) {
        return RevenueShareRuleResponse.from(
                useCase.update(ruleId, req.shareModel(), req.params(),
                        req.sharePercentage(), req.effectiveFrom(), req.effectiveTo(),
                        updatedBy)
        );
    }

    public RevenueShareRuleResponse disable(UUID ruleId, UUID disabledBy) {
        return RevenueShareRuleResponse.from(useCase.disable(ruleId, disabledBy));
    }

    public RevenueShareRuleResponse findActiveByOperatorId(UUID operatorId) {
        return useCase.findActiveByOperatorId(operatorId)
                .map(RevenueShareRuleResponse::from)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.REVENUE_SHARE_RULE_NOT_FOUND));
    }

    public List<RevenueShareRuleResponse> findAllByOperatorId(UUID operatorId) {
        return useCase.findAllByOperatorId(operatorId).stream()
                .map(RevenueShareRuleResponse::from)
                .toList();
    }
}