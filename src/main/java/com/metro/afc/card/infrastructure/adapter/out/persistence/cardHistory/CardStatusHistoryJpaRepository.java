package com.metro.afc.card.infrastructure.adapter.out.persistence.cardHistory;

import com.metro.afc.card.domain.model.CardStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardStatusHistoryJpaRepository
        extends JpaRepository<CardStatusHistory, UUID> {
    List<CardStatusHistory> findByCardIdOrderByChangedAtAsc(UUID cardId);
}