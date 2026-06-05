package com.metro.afc.card.application;

import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService implements CardUseCase {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public Card create(String cardUid, boolean supportsMetro, boolean supportsBus) {
        if (cardRepository.existsByCardUid(cardUid)) {
            throw new ConflictException(ErrorCode.CARD_ALREADY_EXISTS);
        }
        return cardRepository.save(Card.create(cardUid, supportsMetro, supportsBus));
    }

    @Override
    @Transactional
    public Card issue(UUID id, UUID stationId) {
        Card card = findOrThrow(id);
        card.issue(stationId);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card activate(UUID id) {
        Card card = findOrThrow(id);
        card.activate();
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card suspend(UUID id) {
        Card card = findOrThrow(id);
        card.suspend();
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card unsuspend(UUID id) {
        Card card = findOrThrow(id);
        card.unsuspend();
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card revoke(UUID id) {
        Card card = findOrThrow(id);
        card.revoke();
        return cardRepository.save(card);
    }

    @Override
    public Card findById(UUID id) {
        return findOrThrow(id);
    }

    @Override
    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    private Card findOrThrow(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CARD_NOT_FOUND));
    }
}
