package day.ohya.demodd.exception;

import day.ohya.demodd.constant.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode error;
    private final Object[] args;

    public BusinessException(ErrorCode error) {
        super(error.name());
        this.error = error;
        this.args = null;
    }

    public BusinessException(ErrorCode error, Object... args) {
        super(error.name());
        this.error = error;
        this.args = args;
    }
}
