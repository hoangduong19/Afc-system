package com.metro.afc.shared.infrastructure.exception;

public enum ErrorCode {

    // Auth
    INVALID_CREDENTIALS(401, "Sai tên đăng nhập hoặc mật khẩu"),
    UNAUTHORIZED(401,       "Chưa xác thực"),
    FORBIDDEN(403,          "Không có quyền thực hiện thao tác này"),
    TOKEN_INVALID(401,      "Token không hợp lệ"),
    TOKEN_EXPIRED(401,      "Token đã hết hạn"),

    // User
    USER_NOT_FOUND(404,     "Người dùng không tồn tại"),
    USER_ALREADY_EXISTS(409,"Tên đăng nhập hoặc email đã tồn tại"),
    USER_INACTIVE(403,      "Tài khoản đã bị vô hiệu hóa"),

    // Role
    ROLE_NOT_FOUND(404,     "Role không tồn tại"),

    // Card
    CARD_NOT_FOUND(404,     "Thẻ không tồn tại"),
    CARD_BLACKLISTED(403,   "Thẻ đang trong danh sách đen"),
    CARD_INACTIVE(403,      "Thẻ chưa được kích hoạt"),
    CARD_ALREADY_LINKED(409,"Thẻ đã được liên kết với tài khoản khác"),
    CARD_INVALID_TRANSITION(403, "Khong the chuyen format"),

    // Wallet
    WALLET_NOT_FOUND(404,   "Ví không tồn tại"),
    INSUFFICIENT_BALANCE(400,"Số dư không đủ"),

    // Fare
    FARE_RULE_NOT_FOUND(404,"Quy tắc giá vé không tồn tại"),
    FARE_RULE_INACTIVE(400, "Quy tắc giá vé không còn hiệu lực"),

    // Trip
    TRIP_NOT_FOUND(404,     "Chuyến đi không tồn tại"),
    TRIP_ALREADY_STARTED(409,"Thẻ đang có chuyến đi chưa kết thúc"),
    TAP_VALIDATION_FAILED(400,"Không thể thực hiện tap"),

    // Settlement
    SETTLEMENT_NOT_FOUND(404,   "Quyết toán không tồn tại"),
    SETTLEMENT_ALREADY_CONFIRMED(409, "Quyết toán đã được xác nhận"),

    //Operator
    OPERATOR_NOT_FOUND(404, "Không tìm thấy công ty vận hành"),
    OPERATOR_ALREADY_EXISTS(409, "Công ty vận hành đã tồn tại"),

    // Generic
    VALIDATION_ERROR(400,   "Dữ liệu không hợp lệ"),
    INTERNAL_ERROR(500,     "Lỗi hệ thống");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() { return httpStatus; }
    public String getMessage()  { return message; }
}