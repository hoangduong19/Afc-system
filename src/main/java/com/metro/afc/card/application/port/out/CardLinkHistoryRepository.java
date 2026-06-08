package com.metro.afc.card.application.port.out;

import com.metro.afc.card.domain.model.CardLinkHistory;

import java.util.List;
import java.util.UUID;

public interface CardLinkHistoryRepository {
    CardLinkHistory save(CardLinkHistory history);
    List<CardLinkHistory> findByCardId(UUID cardId);
}