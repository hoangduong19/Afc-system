package com.metro.afc.card.domain.events.handler;

import com.metro.afc.card.application.port.out.CardLinkHistoryRepository;
import com.metro.afc.card.domain.events.cardLink.CardLinkedEvent;
import com.metro.afc.card.domain.events.cardLink.CardUnlinkedEvent;
import com.metro.afc.card.domain.model.CardLinkHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CardLinkHistoryEventHandler {

    private final CardLinkHistoryRepository linkHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CardLinkedEvent event) {
        linkHistoryRepository.save(
                CardLinkHistory.linked(
                        event.cardId(), event.userId(), event.performedBy()
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CardUnlinkedEvent event) {
        linkHistoryRepository.save(
                CardLinkHistory.unlinked(
                        event.cardId(), event.previousUserId(), event.performedBy()
                )
        );
    }
}