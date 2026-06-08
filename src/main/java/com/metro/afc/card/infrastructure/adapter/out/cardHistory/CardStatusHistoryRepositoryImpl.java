package com.metro.afc.card.infrastructure.adapter.out.cardHistory;

import com.metro.afc.card.application.port.out.CardStatusHistoryRepository;
import com.metro.afc.card.domain.model.CardStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardStatusHistoryRepositoryImpl implements CardStatusHistoryRepository {

    private final CardStatusHistoryJpaRepository jpa;

    @Override
    public CardStatusHistory save(CardStatusHistory history) {
        return jpa.save(history);
    }

    @Override
    public List<CardStatusHistory> findByCardId(UUID cardId) {
        return jpa.findByCardIdOrderByChangedAtAsc(cardId);
    }
}