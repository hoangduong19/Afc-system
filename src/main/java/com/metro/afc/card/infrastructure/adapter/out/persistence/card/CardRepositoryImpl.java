package com.metro.afc.card.infrastructure.adapter.out.persistence.card;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final CardJpaRepository jpa;

    @Override
    public Optional<Card> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Card> findByCardUid(String cardUid) {
        return jpa.findByCardUid(cardUid);
    }

    @Override
    public boolean existsByCardUid(String cardUid) {
        return jpa.existsByCardUid(cardUid);
    }

    @Override
    public List<Card> findAll() {
        return jpa.findAll();
    }

    @Override
    public Card save(Card card) {
        return jpa.save(card);
    }
}
