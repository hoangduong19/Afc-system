package com.metro.afc.card.infrastructure.adapter.in;

import com.metro.afc.card.application.dto.card.*;
import com.metro.afc.card.application.dto.cardLink.LinkCardRequest;
import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.CardStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardFacade {

    private final CardUseCase cardUseCase;

    public CardResponse create(CreateCardRequest request, UUID createdBy) {
        return CardResponse.from(cardUseCase.create(
                request.cardUid(), request.userId(),
                request.supportsMetro(), request.supportsBus(),
                createdBy
        ));
    }

    public CardResponse issue(UUID id, IssueCardRequest request, UUID changedBy) {
        return CardResponse.from(cardUseCase.issue(id, request.stationId(), changedBy));
    }

    public CardResponse activate(UUID id, UUID changedBy) {
        return CardResponse.from(cardUseCase.activate(id, changedBy));
    }

    public CardResponse suspend(UUID id, CardActionRequest request, UUID changedBy) {
        return CardResponse.from(cardUseCase.suspend(id, request.reason(), changedBy));
    }

    public CardResponse unsuspend(UUID id, CardActionRequest request, UUID changedBy) {
        return CardResponse.from(cardUseCase.unsuspend(id, request.reason(), changedBy));
    }

    public CardResponse revoke(UUID id, CardActionRequest request, UUID changedBy) {
        return CardResponse.from(cardUseCase.revoke(id, request.reason(), changedBy));
    }

    public CardResponse link(UUID id, LinkCardRequest request, UUID performedBy) {
        return CardResponse.from(cardUseCase.link(id, request.userId(), performedBy));
    }

    public CardResponse unlink(UUID id, UUID performedBy) {
        return CardResponse.from(cardUseCase.unlink(id, performedBy));
    }

    public CardResponse getMyCard(String cardUid, UUID userId) {
        return CardResponse.from(cardUseCase.findByCardUidForUser(cardUid, userId));
    }

    public CardResponse suspendMyCard(String cardUid, UUID userId) {
        Card card = cardUseCase.findByCardUidForUser(cardUid, userId);
        return CardResponse.from(
                cardUseCase.suspend(card.getId(), "Suspended by card owner", userId)
        );
    }

    public CardResponse findById(UUID id) {
        return CardResponse.from(cardUseCase.findById(id));
    }

    public CardResponse findByCardUid(String cardUid) {
        return CardResponse.from(cardUseCase.findByCardUid(cardUid));
    }


    public CardDetailResponse findDetailById(UUID id) {
        Card card = cardUseCase.findById(id);
        List<CardStatusHistory> history = cardUseCase.findStatusHistory(id);
        return CardDetailResponse.from(card, history);
    }

    public List<CardResponse> findAll() {
        return cardUseCase.findAll().stream()
                .map(CardResponse::from).toList();
    }
}