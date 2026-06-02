package com.metro.afc.identity.infrastructure.adapter.in.facade;

import com.metro.afc.identity.application.dto.LoginRequest;
import com.metro.afc.identity.application.dto.LoginResponse;
import com.metro.afc.identity.application.dto.RegisterRequest;
import com.metro.afc.identity.domain.model.TokenPair;
import com.metro.afc.identity.domain.port.in.AuthUseCase;
import com.metro.afc.shared.domain.valueobject.Email;
import com.metro.afc.shared.domain.valueobject.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthUseCase authUseCase;

    public LoginResponse login(LoginRequest request) {
        Username username = Username.of(request.username());

        TokenPair tokens = authUseCase.authenticate(
                username,
                request.password()
        );

        return LoginResponse.of(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn()
        );
    }

    public LoginResponse refresh(String refreshToken) {
        TokenPair tokens = authUseCase.refresh(refreshToken);

        return LoginResponse.of(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn()
        );
    }

    public void register(RegisterRequest request) {
        authUseCase.register(
                Username.of(request.username()),
                Email.of(request.email()),
                request.password(),   // raw, service sẽ encode
                request.fullName(),
                request.phone()
        );
    }

    public void logout(String refreshToken) {
        authUseCase.logout(refreshToken);
    }
}