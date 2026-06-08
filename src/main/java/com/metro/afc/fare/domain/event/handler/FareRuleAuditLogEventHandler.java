package com.metro.afc.fare.domain.event.handler;

import com.metro.afc.fare.application.port.out.FareRuleAuditLogRepository;
import com.metro.afc.fare.domain.event.fareRule.FareRuleCreatedEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleDisabledEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleUpdatedEvent;
import com.metro.afc.fare.domain.model.FareRuleAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FareRuleAuditLogEventHandler {

    private final FareRuleAuditLogRepository auditLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(FareRuleCreatedEvent event) {
        auditLogRepository.save(FareRuleAuditLog.created(
                event.fareRuleId(),
                event.snapshot(),
                event.createdBy()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(FareRuleUpdatedEvent event) {
        auditLogRepository.save(FareRuleAuditLog.updated(
                event.newFareRuleId(),
                event.oldSnapshot(),
                event.newSnapshot(),
                event.reason(),
                event.updatedBy()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(FareRuleDisabledEvent event) {
        auditLogRepository.save(FareRuleAuditLog.disabled(
                event.fareRuleId(),
                event.oldSnapshot(),
                event.reason(),
                event.disabledBy()
        ));
    }
}