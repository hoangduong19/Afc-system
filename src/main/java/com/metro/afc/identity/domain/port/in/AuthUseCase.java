package com.metro.afc.identity.domain.port.in;

import com.metro.afc.identity.domain.model.TokenPair;
import com.metro.afc.shared.domain.valueobject.Email;
import com.metro.afc.shared.domain.valueobject.Username;

public interface AuthUseCase {
    TokenPair authenticate(Username username, String rawPassword);
    TokenPair refresh(String refreshToken);
    void logout(String refreshToken);
    void register(Username username, Email email, String encodedPassword, String fullName, String phone);
}
