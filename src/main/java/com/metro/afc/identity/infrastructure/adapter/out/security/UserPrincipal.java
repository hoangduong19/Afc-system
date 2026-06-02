package com.metro.afc.identity.infrastructure.adapter.out.security;

import com.metro.afc.identity.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(UUID id, String username, String password,
                          boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id          = id;
        this.username    = username;
        this.password    = password;
        this.enabled     = enabled;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // ROLE_ACC_ADMIN, ROLE_STATION_STAFF... → dùng với hasRole()
        user.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()))
        );

        // CARD_ISSUE, FARE_CREATE... → dùng với hasAuthority()
        user.getAllPermissionCodes().forEach(code ->
                authorities.add(new SimpleGrantedAuthority(code))
        );

        return new UserPrincipal(
                user.getId(),
                user.getUsernameValue(),
                user.getPasswordHash(),
                user.isActive(),
                Collections.unmodifiableList(authorities)
        );
    }

    // ── UserDetails ──────────────────────────────────────────────

    @Override public String getUsername()   { return id.toString(); }
    @Override public String getPassword()   { return password; }
    @Override public boolean isEnabled()    { return enabled; }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
