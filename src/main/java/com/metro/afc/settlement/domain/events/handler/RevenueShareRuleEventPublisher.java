package com.metro.afc.settlement.domain.events.handler;

import com.metro.afc.settlement.domain.RevenueShareRule;
import com.metro.afc.settlement.domain.RevenueShareRuleAuditLog;
import com.metro.afc.settlement.domain.events.RevenueShareRuleCreatedEvent;
import com.metro.afc.settlement.domain.events.RevenueShareRuleDisabledEvent;
import com.metro.afc.settlement.infrastructure.adapter.out.revenueShareRuleAuditLog.RevenueShareRuleAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RevenueShareRuleEventPublisher {

    private final RevenueShareRuleAuditJpaRepository auditRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RevenueShareRuleCreatedEvent event) {
        RevenueShareRule r = event.rule();
        auditRepository.save(RevenueShareRuleAuditLog.of(
                r.getId(), "CREATED", null,
                Map.of("shareModel", r.getShareModel(),
                        "sharePercentage", r.getSharePercentage(),
                        "effectiveFrom", r.getEffectiveFrom()),
                r.getCreatedBy()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RevenueShareRuleDisabledEvent event) {
        RevenueShareRule r = event.rule();
        auditRepository.save(RevenueShareRuleAuditLog.of(
                r.getId(), "DISABLED", null, null, event.disabledBy()
        ));
    }
}