package com.metro.afc.card.application.port.out;

import com.metro.afc.card.domain.model.Card;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository {
    Optional<Card> findById(UUID id);
    Optional<Card> findByCardUid(String cardUid);
    boolean existsByCardUid(String cardUid);
    boolean existsById(UUID id);
    List<Card> findAll();
    Card save(Card card);
}
