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
    CARD_NOT_LINKED(400,     "Card is not linked to any user"),

    // Ticket
    TICKET_NOT_FOUND(404,       "Ticket not found"),
    TICKET_INVALID_STATUS(400,  "Invalid ticket status"),
    TICKET_ALREADY_LINKED(409,  "Ticket already linked to a card"),
    CARD_NOT_ACTIVE(400,        "Card must be ACTIVE"),
    INVALID_PASS_SCOPE(400, "Invalid pass scope"),

    // Wallet
    WALLET_NOT_FOUND(404,   "Wallet not found"),
    INSUFFICIENT_BALANCE(400,"Insufficient balance"),

    // Fare
    FARE_RULE_NOT_FOUND(404,    "Fare rule not found"),
    FARE_RULE_INACTIVE(400,     "Fare rule is no longer active"),
    FARE_RULE_ALREADY_EXISTS(409, "Fare rule code already exists"),
    INVALID_FARE_AMOUNT(400, "Corrected fare must be non-negative"),

    // Trip
    TRIP_NOT_FOUND(404,     "Trip not found"),
    TRIP_ALREADY_STARTED(409,"Card currently has an incomplete trip"),
    TAP_VALIDATION_FAILED(400,"Tap validation failed"),

    // Settlement
    SETTLEMENT_NOT_FOUND(404,   "Settlement not found"),
    SETTLEMENT_ALREADY_CONFIRMED(409, "Settlement has already been confirmed"),
    SETTLEMENT_ALREADY_EXISTS(409, "Settlement already exists for this period"),
    SETTLEMENT_NOT_PENDING(400,    "Settlement is not in DRAFT status"),
    SETTLEMENT_NO_TRIPS(400,       "No completed trips found in this period"),
    SETTLEMENT_RECONCILE_FAIL(400, "Cannot confirm: reconciliation MISMATCH"),
    SETTLEMENT_HAS_UNRESOLVED_ANOMALIES(400,
            "Cannot run settlement: unresolved anomalies exist in this period"),

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
    STATION_SAME(400, "From and to station must be different"),


    // Blacklist
    BLACKLIST_NOT_FOUND(404,        "Blacklist entry not found"),
    CARD_ALREADY_BLACKLISTED(409,   "Card is already blacklisted"),
    BLACKLIST_ALREADY_REMOVED(400,  "Card is not in active blacklist"),

    // FareDiscount
    FARE_DISCOUNT_NOT_FOUND(404,   "Fare discount not found"),
    FARE_DISCOUNT_INACTIVE(400,    "Fare discount is already inactive"),
    FARE_DISCOUNT_ALREADY_EXISTS(409, "Active discount for this passenger type already exists"),

    // RevenueShareRule
    REVENUE_SHARE_RULE_NOT_FOUND(404,         "Revenue share rule not found"),
    REVENUE_SHARE_RULE_ALREADY_EXISTS(409,    "Operator already has an active revenue share rule"),
    REVENUE_SHARE_RULE_ALREADY_INACTIVE(400,  "Revenue share rule is already inactive"),
    REVENUE_SHARE_RULE_INVALID(400,           "Invalid revenue share rule configuration"),

    // Anomaly
    ANOMALY_NOT_FOUND(404,          "Anomaly not found"),
    ANOMALY_ALREADY_RESOLVED(400,   "Anomaly is already resolved"),

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