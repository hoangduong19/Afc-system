package com.metro.afc.shared.infrastructure.exception;

public class BusinessRuleException extends AfcException {
    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }
    public BusinessRuleException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
