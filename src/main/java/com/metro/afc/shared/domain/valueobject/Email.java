package com.metro.afc.shared.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "Email không được null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value.trim().toLowerCase());
    }
}