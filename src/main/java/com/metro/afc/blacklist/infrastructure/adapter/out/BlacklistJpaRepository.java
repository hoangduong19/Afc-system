package com.metro.afc.blacklist.infrastructure.adapter.out;

import com.metro.afc.blacklist.domain.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistJpaRepository extends JpaRepository<Blacklist, UUID> {
    Optional<Blacklist> findByCardIdAndIsActiveTrue(UUID cardId);
    List<Blacklist> findAllByIsActiveTrue();
    boolean existsByCardIdAndIsActiveTrue(UUID cardId);
}