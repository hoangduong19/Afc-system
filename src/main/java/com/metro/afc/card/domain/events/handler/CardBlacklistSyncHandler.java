package com.metro.afc.card.domain.events.handler;

import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.blacklist.domain.Blacklist;
import com.metro.afc.blacklist.domain.events.BlacklistAddedEvent;
import com.metro.afc.blacklist.domain.events.BlacklistRemovedEvent;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.events.cardStatus.CardStatusChangedEvent;
import com.metro.afc.card.domain.model.enums.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CardBlacklistSyncHandler {

    private final BlacklistRepository blacklistRepository;
    private final CardRepository cardRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CardStatusChangedEvent event) {
        if (event.toStatus() == CardStatus.SUSPENDED
                || event.toStatus() == CardStatus.REVOKED) {
            if (!blacklistRepository.existsActiveByCardId(event.cardId())) {
                blacklistRepository.save(
                        Blacklist.add(event.cardId(), event.reason(), event.changedBy())
                );
            }
        }

        if (event.toStatus() == CardStatus.ACTIVE) {
            blacklistRepository.findActiveByCardId(event.cardId())
                    .ifPresent(b -> {
                        b.remove(event.changedBy());
                        blacklistRepository.save(b);
                    });
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(BlacklistRemovedEvent event) {
        cardRepository.findById(event.cardId())
                .filter(card -> card.getStatus() == CardStatus.SUSPENDED)
                .ifPresent(card -> {
                    card.activate(event.removedBy());
                    cardRepository.save(card);
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(BlacklistAddedEvent event) {
        cardRepository.findById(event.cardId())
                .filter(card -> card.getStatus() == CardStatus.ACTIVE)
                .ifPresent(card -> {
                    card.suspend(event.reason(), event.addedBy());
                    cardRepository.save(card);
                });
    }
}