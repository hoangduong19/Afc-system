package com.metro.afc.identity.application;

import com.metro.afc.identity.domain.model.RefreshToken;
import com.metro.afc.identity.domain.model.Role;
import com.metro.afc.identity.domain.model.TokenPair;
import com.metro.afc.identity.domain.model.User;
import com.metro.afc.identity.domain.port.in.AuthUseCase;
import com.metro.afc.identity.domain.port.out.repository.RefreshTokenRepository;
import com.metro.afc.identity.domain.port.out.repository.RoleRepository;
import com.metro.afc.identity.domain.port.out.repository.UserRepository;
import com.metro.afc.identity.infrastructure.adapter.out.security.JwtUtil;
import com.metro.afc.identity.infrastructure.adapter.out.security.UserDetailsServiceImpl;
import com.metro.afc.identity.infrastructure.adapter.out.security.UserPrincipal;
import com.metro.afc.shared.domain.valueobject.Email;
import com.metro.afc.shared.domain.valueobject.Username;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.shared.infrastructure.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// identity/application/service/AuthService.java
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    // ── Login ────────────────────────────────────────────────────

    @Override
    @Transactional
    public TokenPair authenticate(Username username, String rawPassword) {

        // 1. Xác thực username + password qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username.value(),
                        rawPassword
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // 2. Xóa refresh token cũ nếu có
        refreshTokenRepository.deleteByUserId(principal.getId());

        // 3. Sinh token mới
        String accessToken  = jwtUtil.generateAccessToken(principal);
        String refreshToken = jwtUtil.generateRefreshToken();

        // 4. Lưu refresh token vào DB
        refreshTokenRepository.save(
                RefreshToken.create(
                        principal.getId(),
                        refreshToken,
                        jwtUtil.refreshTokenExpiresAt()
                )
        );

        return new TokenPair(
                accessToken,
                refreshToken,
                jwtUtil.accessTokenExpirationMs()
        );
    }

    // ── Refresh ──────────────────────────────────────────────────

    @Override
    @Transactional
    public TokenPair refresh(String refreshToken) {

        // 1. Tìm refresh token trong DB
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.TOKEN_INVALID));

        // 2. Kiểm tra hết hạn
        if (stored.isExpired()) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new UnauthorizedException(ErrorCode.TOKEN_EXPIRED);
        }

        // 3. Load user
        UserDetails userDetails = userDetailsService.loadUserById(stored.getUserId());
        UserPrincipal principal = (UserPrincipal) userDetails;

        // 4. Xóa refresh token cũ, sinh cái mới — rotation
        refreshTokenRepository.deleteByToken(refreshToken);

        String newAccessToken  = jwtUtil.generateAccessToken(principal);
        String newRefreshToken = jwtUtil.generateRefreshToken();

        refreshTokenRepository.save(
                RefreshToken.create(
                        principal.getId(),
                        newRefreshToken,
                        jwtUtil.refreshTokenExpiresAt()
                )
        );

        return new TokenPair(
                newAccessToken,
                newRefreshToken,
                jwtUtil.accessTokenExpirationMs()
        );
    }

    // ── Logout ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Override
    @Transactional
    public void register(Username username, Email email, String rawPassword,
                         String fullName, String phone) {
        if (userRepository.existsByUsername(username.value())) {
            throw new ConflictException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(email.value())) {
            throw new ConflictException(ErrorCode.USER_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByCode("PASSENGER")
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.create(
                username.value(),
                email.value(),
                passwordEncoder.encode(rawPassword),
                fullName,
                phone
        );
        user.assignRole(role);
        userRepository.save(user);
    }
}
