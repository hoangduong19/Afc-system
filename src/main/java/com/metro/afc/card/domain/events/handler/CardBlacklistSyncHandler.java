package com.metro.afc.card.domain.events.handler;

import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.blacklist.domain.Blacklist;
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
}