package com.metro.afc.shared.infrastructure.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        List<FieldError> errors
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                Instant.now(),
                null
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(
                errorCode.name(),
                detail,
                Instant.now(),
                null
        );
    }

    public static ErrorResponse withFields(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                Instant.now(),
                errors
        );
    }
}