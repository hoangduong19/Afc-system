package com.metro.afc.card.domain.events.handler;

import com.metro.afc.card.application.port.out.CardStatusHistoryRepository;
import com.metro.afc.card.domain.events.cardStatus.CardStatusChangedEvent;
import com.metro.afc.card.domain.model.CardStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CardStatusHistoryEventHandler {

    private final CardStatusHistoryRepository historyRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CardStatusChangedEvent event) {
        historyRepository.save(CardStatusHistory.of(
                event.cardId(),
                event.fromStatus(),
                event.toStatus(),
                event.reason(),
                event.changedBy()
        ));
    }
}