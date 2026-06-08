package com.metro.afc.blacklist.application.port.out;

import com.metro.afc.blacklist.domain.Blacklist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlacklistRepository {
    Blacklist save(Blacklist blacklist);
    Optional<Blacklist> findById(UUID id);
    Optional<Blacklist> findActiveByCardId(UUID cardId);
    List<Blacklist> findAllActive();
    boolean existsActiveByCardId(UUID cardId);
}