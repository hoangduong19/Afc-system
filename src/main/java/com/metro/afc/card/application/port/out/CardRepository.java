package com.metro.afc.card.application.port.out;

import com.metro.afc.card.domain.model.Card;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository {
    Optional<Card> findById(UUID id);
    Optional<Card> findByCardUid(String cardUid);
    List<Card> findByCardUidIn(Collection<String> cardUids);
    boolean existsByCardUid(String cardUid);
    boolean existsById(UUID id);
    List<Card> findByLinkedUserId(UUID userId);
    List<Card> findAll();
    Card save(Card card);
    List<Card> saveAll(List<Card> cards);
}
