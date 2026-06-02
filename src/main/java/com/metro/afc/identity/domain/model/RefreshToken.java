package com.metro.afc.identity.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory method ───────────────────────────────────────────

    public static RefreshToken create(UUID userId, String token, Instant expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.userId    = userId;
        rt.token     = token;
        rt.expiresAt = expiresAt;
        return rt;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}