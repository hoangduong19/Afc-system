package com.metro.afc.identity.infrastructure.adapter.out.repository;

import com.metro.afc.identity.domain.model.RefreshToken;
import com.metro.afc.identity.domain.port.out.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpa;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpa.findByToken(token);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpa.save(refreshToken);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpa.deleteByUserId(userId);
    }

    @Override
    public void deleteByToken(String token) {
        jpa.deleteByToken(token);
    }
}