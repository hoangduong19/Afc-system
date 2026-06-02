package com.metro.afc.shared.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Username(String value) {

    private static final int MIN = 3;
    private static final int MAX = 100;

    public Username {
        Objects.requireNonNull(value, "Username không được null");
        if (value.isBlank() || value.length() < MIN || value.length() > MAX) {
            throw new IllegalArgumentException(
                    "Username phải từ %d đến %d ký tự".formatted(MIN, MAX)
            );
        }
    }

    public static Username of(String value) {
        return new Username(value.trim());
    }
}