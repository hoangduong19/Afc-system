package com.metro.afc.card.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCardRequest(
        @NotBlank(message = "Card UID không được để trống")
        @Size(max = 100, message = "Card UID tối đa 100 ký tự")
        String cardUid,

        boolean supportsMetro,
        boolean supportsBus
) {
    public RegisterCardRequest {
        if (!supportsMetro && !supportsBus) {
            throw new IllegalArgumentException("Thẻ phải hỗ trợ ít nhất một loại phương tiện");
        }
    }
}