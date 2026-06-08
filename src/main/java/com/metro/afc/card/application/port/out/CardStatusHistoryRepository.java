package com.metro.afc.card.application.port.out;

import com.metro.afc.card.domain.model.CardStatusHistory;

import java.util.List;
import java.util.UUID;

public interface CardStatusHistoryRepository {
    CardStatusHistory save(CardStatusHistory history);
    List<CardStatusHistory> findByCardId(UUID cardId);
}