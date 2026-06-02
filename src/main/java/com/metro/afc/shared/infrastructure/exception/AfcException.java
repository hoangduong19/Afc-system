package com.metro.afc.shared.infrastructure.exception;

public class AfcException extends RuntimeException {

    private final ErrorCode errorCode;

    public AfcException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AfcException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}