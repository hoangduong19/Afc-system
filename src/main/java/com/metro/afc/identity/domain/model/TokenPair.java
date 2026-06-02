package com.metro.afc.identity.domain.model;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}