package com.metro.afc.card.application.port.in;

import com.metro.afc.card.domain.model.Card;

import java.util.List;
import java.util.UUID;

public interface CardUseCase {
    Card create(String cardUid, boolean supportsMetro, boolean supportsBus);
    Card issue(UUID id, UUID stationId);
    Card activate(UUID id);
    Card suspend(UUID id);
    Card unsuspend(UUID id);
    Card revoke(UUID id);
    Card findById(UUID id);
    List<Card> findAll();
}