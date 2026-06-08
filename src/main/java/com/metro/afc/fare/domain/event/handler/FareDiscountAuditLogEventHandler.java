package com.metro.afc.fare.domain.event.handler;

import com.metro.afc.fare.application.port.out.FareDiscountAuditLogRepository;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountCreatedEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountDisabledEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountUpdatedEvent;
import com.metro.afc.fare.domain.model.FareDiscountAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FareDiscountAuditLogEventHandler {

    private final FareDiscountAuditLogRepository auditLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(FareDiscountCreatedEvent event) {
        auditLogRepository.save(FareDiscountAuditLog.of(
                event.discountId(), "CREATED",
                null, event.snapshot(), event.createdBy()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(FareDiscountUpdatedEvent event) {
        auditLogRepository.save(FareDiscountAuditLog.of(
                event.discountId(), "UPDATED",
                event.oldSnapshot(), event.newSnapshot(), event.updatedBy()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(FareDiscountDisabledEvent event) {
        auditLogRepository.save(FareDiscountAuditLog.of(
                event.discountId(), "DISABLED",
                event.oldSnapshot(), null, event.disabledBy()
        ));
    }
}