package com.metro.afc.card.application.port.in;

import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.CardStatusHistory;
import com.metro.afc.card.domain.model.enums.CardType;

import java.util.List;
import java.util.UUID;

public interface CardUseCase {
    Card create(String cardUid, CardType type, UUID userId,
                Boolean supportsMetro, Boolean supportsBus, UUID createdBy);
    Card issue(UUID id, UUID stationId, UUID changedBy);
    Card activate(UUID id, UUID changedBy);
    Card suspend(UUID id, String reason, UUID changedBy);
    Card unsuspend(UUID id, String reason, UUID changedBy);
    Card revoke(UUID id, String reason, UUID changedBy);
    Card findById(UUID id);
    Card findByCardUid(String cardUid);
    List<CardStatusHistory> findStatusHistory(UUID cardId);
    List<Card> findAll();
}