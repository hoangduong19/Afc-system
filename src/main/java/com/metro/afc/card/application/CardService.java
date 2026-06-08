package com.metro.afc.card.application;

import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.enums.CardType;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService implements CardUseCase {

    private final CardRepository     cardRepository;
    private final StationRepository stationRepository;

    @Override
    @Transactional
    public Card create(String cardUid, CardType type, UUID userId,
                       Boolean supportsMetro, Boolean supportsBus,
                       UUID createdBy) {
        if (cardRepository.existsByCardUid(cardUid.trim().toUpperCase())) {
            throw new ConflictException(ErrorCode.CARD_ALREADY_EXISTS);
        }
        return cardRepository.save(
                Card.create(cardUid, type, userId, supportsMetro, supportsBus, createdBy)
        );
    }

    @Override
    @Transactional
    public Card issue(UUID id, UUID stationId, UUID changedBy) {
        Card card = findOrThrow(id);
        if (!stationRepository.existsById(stationId)) {
            throw new NotFoundException(ErrorCode.STATION_NOT_FOUND);
        }
        card.issue(stationId, changedBy);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card activate(UUID id, UUID changedBy) {
        Card card = findOrThrow(id);
        card.activate(changedBy);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card suspend(UUID id, String reason, UUID changedBy) {
        Card card = findOrThrow(id);
        card.suspend(reason, changedBy);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card unsuspend(UUID id, String reason, UUID changedBy) {
        Card card = findOrThrow(id);
        card.unsuspend(reason, changedBy);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card revoke(UUID id, String reason, UUID changedBy) {
        Card card = findOrThrow(id);
        card.revoke(reason, changedBy);
        return cardRepository.save(card);
    }

    @Override
    public Card findById(UUID id) {
        return findOrThrow(id);
    }

    @Override
    public Card findByCardUid(String cardUid) {
        return cardRepository.findByCardUid(cardUid.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CARD_NOT_FOUND));
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