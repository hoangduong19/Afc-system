package com.metro.afc.card.infrastructure.adapter.out.persistence.cardLink;

import com.metro.afc.card.application.port.out.CardLinkHistoryRepository;
import com.metro.afc.card.domain.model.CardLinkHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardLinkHistoryRepositoryImpl implements CardLinkHistoryRepository {

    private final CardLinkHistoryJpaRepository jpa;

    @Override
    public CardLinkHistory save(CardLinkHistory history) {
        return jpa.save(history);
    }

    @Override
    public List<CardLinkHistory> findByCardId(UUID cardId) {
        return jpa.findByCardIdOrderByPerformedAtAsc(cardId);
    }
}