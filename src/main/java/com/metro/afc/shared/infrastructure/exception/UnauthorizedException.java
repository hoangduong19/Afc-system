package com.metro.afc.shared.infrastructure.exception;

public class UnauthorizedException extends AfcException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}