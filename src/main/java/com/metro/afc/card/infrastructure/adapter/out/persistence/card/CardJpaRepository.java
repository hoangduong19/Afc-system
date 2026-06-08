package com.metro.afc.card.infrastructure.adapter.out.persistence.card;

import com.metro.afc.card.domain.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardJpaRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCardUid(String cardUid);
    boolean existsByCardUid(String cardUid);
}
