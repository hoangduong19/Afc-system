package com.metro.afc.card.infrastructure.adapter.in;

import com.metro.afc.card.application.dto.CardResponse;
import com.metro.afc.card.application.dto.IssueCardRequest;
import com.metro.afc.card.application.dto.RegisterCardRequest;
import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.card.domain.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardFacade {

    private final CardUseCase cardUseCase;

    public CardResponse create(RegisterCardRequest request) {
        Card card = cardUseCase.create(
                request.cardUid(),
                request.supportsMetro(),
                request.supportsBus()
        );
        return CardResponse.from(card);
    }

    public CardResponse issue(UUID id, IssueCardRequest request) {
        Card card = cardUseCase.issue(id, request.stationId());
        return CardResponse.from(card);
    }

    public CardResponse activate(UUID id) {
        Card card = cardUseCase.activate(id);
        return CardResponse.from(card);
    }

    public CardResponse suspend(UUID id) {
        Card card = cardUseCase.suspend(id);
        return CardResponse.from(card);
    }

    public CardResponse unsuspend(UUID id) {
        Card card = cardUseCase.unsuspend(id);
        return CardResponse.from(card);
    }

    public CardResponse revoke(UUID id) {
        Card card = cardUseCase.revoke(id);
        return CardResponse.from(card);
    }

    public CardResponse findById(UUID id) {
        return CardResponse.from(cardUseCase.findById(id));
    }

    public List<CardResponse> findAll() {
        return cardUseCase.findAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }
}