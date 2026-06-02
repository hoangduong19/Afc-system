package com.metro.afc.shared.infrastructure.exception;

public class NotFoundException extends AfcException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
