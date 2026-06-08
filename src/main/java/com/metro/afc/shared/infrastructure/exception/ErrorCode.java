package com.metro.afc.shared.infrastructure.exception;

public enum ErrorCode {

    // Auth
    INVALID_CREDENTIALS(401, "Invalid username or password"),
    UNAUTHORIZED(401,       "Unauthorized access"),
    FORBIDDEN(403,          "You do not have permission to perform this action"),
    TOKEN_INVALID(401,      "Invalid token"),
    TOKEN_EXPIRED(401,      "Token has expired"),

    // User
    USER_NOT_FOUND(404,     "User not found"),
    USER_ALREADY_EXISTS(409,"Username or email already exists"),
    USER_INACTIVE(403,      "Account has been deactivated"),

    // Role
    ROLE_NOT_FOUND(404,     "Role not found"),

    // Card
    CARD_NOT_FOUND(404,     "Card not found"),
    CARD_BLACKLISTED(403,   "Card is blacklisted"),
    CARD_INACTIVE(403,      "Card is not activated"),
    CARD_ALREADY_LINKED(409,"Card is already linked to another account"),
    CARD_ALREADY_EXISTS(409,      "Card ID already exists"),
    CARD_INVALID_TRANSITION(400,  "Invalid card status transition"),

    // Wallet
    WALLET_NOT_FOUND(404,   "Wallet not found"),
    INSUFFICIENT_BALANCE(400,"Insufficient balance"),

    // Fare
    FARE_RULE_NOT_FOUND(404,    "Fare rule not found"),
    FARE_RULE_INACTIVE(400,     "Fare rule is no longer active"),
    FARE_RULE_ALREADY_EXISTS(409, "Fare rule code already exists"),

    // Trip
    TRIP_NOT_FOUND(404,     "Trip not found"),
    TRIP_ALREADY_STARTED(409,"Card currently has an incomplete trip"),
    TAP_VALIDATION_FAILED(400,"Tap validation failed"),

    // Settlement
    SETTLEMENT_NOT_FOUND(404,   "Settlement not found"),
    SETTLEMENT_ALREADY_CONFIRMED(409, "Settlement has already been confirmed"),

    // Operator
    OPERATOR_NOT_FOUND(404, "Transit operator not found"),
    OPERATOR_ALREADY_EXISTS(409, "Transit operator already exists"),

    //Route
    ROUTE_NOT_FOUND(404,      "Route not found"),
    ROUTE_ALREADY_EXISTS(409, "Route code already exists"),
    ROUTE_HAS_STATIONS(400,   "Route has stations, cannot be deleted"),

    //Stationn
    STATION_NOT_FOUND(404,          "Station not found"),
    STATION_ALREADY_EXISTS(409,     "Station code already exists"),
    STATION_ORDER_DUPLICATE(409,    "Station order already exists in this route"),
    STATION_KM_MARKER_INVALID(400,  "km_marker must be in ascending order"),

    // Generic
    VALIDATION_ERROR(400,   "Invalid input data"),
    INTERNAL_ERROR(500,     "Internal server error");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() { return httpStatus; }
    public String getMessage()  { return message; }
}