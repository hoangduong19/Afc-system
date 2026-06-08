package com.metro.afc.blacklist.infrastructure.adapter.out;

import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.blacklist.domain.Blacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlacklistRepositoryImpl implements BlacklistRepository {

    private final BlacklistJpaRepository jpa;

    @Override
    public Blacklist save(Blacklist blacklist) {
        return jpa.save(blacklist);
    }

    @Override
    public Optional<Blacklist> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Blacklist> findActiveByCardId(UUID cardId) {
        return jpa.findByCardIdAndIsActiveTrue(cardId);
    }

    @Override
    public List<Blacklist> findAllActive() {
        return jpa.findAllByIsActiveTrue();
    }

    @Override
    public boolean existsActiveByCardId(UUID cardId) {
        return jpa.existsByCardIdAndIsActiveTrue(cardId);
    }
}
