package day.ohya.demodd.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    LOGIN_FAILED(1001, "error.auth.login-failed"),
    USER_NOT_FOUND(1002, "error.auth.user-not-found"),
    EMAIL_ALREADY_EXISTS(1003, "error.auth.email-already-exists"),
    VERIFY_TOKEN_ERROR(1004, "error.auth.verify-token-error"),
    VERIFY_TOKEN_EXPIRE(1005, "error.auth.verify-token-expire"),
    VERIFY_OVER_ATTEMPTS(1006, "error.auth.verify-over-attempts"),

    SYSTEM_ERROR(9000, "error.global.system-error"),
    INVALID_ARGUMENT(4000, "error.global.invalid-argument");

    private final int code;
    private final String messageKey;
}