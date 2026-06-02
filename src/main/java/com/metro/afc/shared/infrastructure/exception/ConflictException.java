package com.metro.afc.shared.infrastructure.exception;

public class ConflictException extends AfcException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
