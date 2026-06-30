package com.metro.afc.passenger;

import com.metro.afc.fare.domain.event.fareRule.FareRuleCreatedEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleDisabledEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleUpdatedEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountCreatedEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountDisabledEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FareCatalogCacheEvictionListener {

    private final FareCatalogService fareCatalogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareRuleCreatedEvent event) {
        evictFareRuleCaches();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareRuleUpdatedEvent event) {
        evictFareRuleCaches();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareRuleDisabledEvent event) {
        evictFareRuleCaches();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareDiscountCreatedEvent event) {
        fareCatalogService.evictDiscountsCache();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareDiscountUpdatedEvent event) {
        fareCatalogService.evictDiscountsCache();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareDiscountDisabledEvent event) {
        fareCatalogService.evictDiscountsCache();
    }

    private void evictFareRuleCaches() {
        fareCatalogService.evictPricesCache();
        fareCatalogService.evictFareRuleMapCache();
    }
}