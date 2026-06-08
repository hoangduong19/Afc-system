package com.metro.afc.card.infrastructure.adapter.out.persistence.cardLink;

import com.metro.afc.card.domain.model.CardLinkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardLinkHistoryJpaRepository
        extends JpaRepository<CardLinkHistory, UUID> {
    List<CardLinkHistory> findByCardIdOrderByPerformedAtAsc(UUID cardId);
}
