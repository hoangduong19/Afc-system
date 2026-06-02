package com.metro.afc.identity.domain.model;

import com.metro.afc.identity.domain.model.enums.UserStatus;
import com.metro.afc.shared.domain.valueobject.Email;
import com.metro.afc.shared.domain.valueobject.Username;
import com.metro.afc.shared.infrastructure.exception.AfcException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, unique = true, length = 100))
    private Username username;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 255))
    private Email email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Factory method ───────────────────────────────────────────

    public static User create(String username, String email,
                              String passwordHash, String fullName,
                              String phone) {
        User user = new User();
        user.username   = Username.of(username);
        user.email      = Email.of(email);
        user.passwordHash = passwordHash;
        user.fullName   = fullName;
        user.phone      = phone;
        user.status     = UserStatus.ACTIVE;
        return user;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public void assignRole(Role role) {
        this.roles.add(role);
    }

    public void revokeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(r -> r.getCode().equals(roleCode));
    }

    public boolean hasPermission(String permissionCode) {
        return roles.stream()
                .anyMatch(r -> r.hasPermission(permissionCode));
    }

    public Set<String> getAllPermissionCodes() {
        return roles.stream()
                .flatMap(r -> r.getPermissionCodes().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new AfcException(
                    ErrorCode.VALIDATION_ERROR, "User đã ở trạng thái ACTIVE"
            );
        }
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new AfcException(
                    ErrorCode.VALIDATION_ERROR, "User đã ở trạng thái INACTIVE"
            );
        }
        this.status = UserStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public void changePassword(String newPasswordHash) {
        Objects.requireNonNull(newPasswordHash, "Password không được null");
        this.passwordHash = newPasswordHash;
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = fullName;
        this.phone    = phone;
    }

    // ── Getters cho Value Objects ────────────────────────────────

    public String getUsernameValue() { return username.value(); }
    public String getEmailValue()    { return email.value(); }

    // ── JPA lifecycle ────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
