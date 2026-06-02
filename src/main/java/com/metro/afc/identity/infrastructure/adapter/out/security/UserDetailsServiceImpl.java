package com.metro.afc.identity.infrastructure.adapter.out.security;

import com.metro.afc.identity.domain.model.User;
import com.metro.afc.identity.domain.port.out.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security gọi cái này lúc login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User không tồn tại: " + username
                ));

        if (!user.isActive()) {
            throw new DisabledException("Tài khoản đã bị vô hiệu hóa");
        }

        return UserPrincipal.from(user);
    }

    // JwtAuthFilter gọi cái này lúc validate token
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User không tồn tại: " + id
                ));

        if (!user.isActive()) {
            throw new DisabledException("Tài khoản đã bị vô hiệu hóa");
        }

        return UserPrincipal.from(user);
    }
}