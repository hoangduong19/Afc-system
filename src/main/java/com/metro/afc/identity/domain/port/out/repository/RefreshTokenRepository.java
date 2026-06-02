package com.metro.afc.identity.domain.port.out.repository;

import com.metro.afc.identity.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
    void deleteByUserId(UUID userId);
    void deleteByToken(String token);
}
