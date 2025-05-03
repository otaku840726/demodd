package day.ohya.demodd.model;

import day.ohya.demodd.constant.ErrorCode;
import lombok.Getter;

@Getter
public class DemoDDApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ApiError error;

    private DemoDDApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.error = null;
    }

    private DemoDDApiResponse(ApiError error) {
        this.success = false;
        this.data = null;
        this.error = error;
    }

    public static <T> DemoDDApiResponse<T> success(T data) {
        return new DemoDDApiResponse<>(data);
    }

    public static DemoDDApiResponse<Void> success() {
        return new DemoDDApiResponse<>((Void) null);
    }

    public static DemoDDApiResponse<Void> fail(ErrorCode errorCode) {
        return new DemoDDApiResponse<>(new ApiError(errorCode.getCode(), errorCode.getMessageKey()));
    }

    public static DemoDDApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new DemoDDApiResponse<>(new ApiError(errorCode.getCode(), message));
    }

    public static DemoDDApiResponse<Void> fail(int code, String message) {
        return new DemoDDApiResponse<>(new ApiError(code, message));
    }

    public record ApiError(int code, String message) {}
}