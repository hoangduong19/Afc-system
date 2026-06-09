package com.metro.afc.blacklist.infrastructure.adapter.out;

import com.metro.afc.blacklist.domain.Blacklist;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistJpaRepository extends JpaRepository<Blacklist, UUID> {
    Optional<Blacklist> findByCardIdAndIsActiveTrue(UUID cardId);
    List<Blacklist> findAllByIsActiveTrue();
    boolean existsByCardIdAndIsActiveTrue(UUID cardId);

    @Query("SELECT b FROM Blacklist b WHERE b.addedAt >= :since " +
            "OR b.removedAt >= :since")
    List<Blacklist> findChangedSince(@Param("since") Instant since);
}