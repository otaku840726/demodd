package day.ohya.demodd.exception;

import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.model.DemoDDApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiValidationExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DemoDDApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String field = fieldError != null ? fieldError.getField() : "unknown";
        String message = fieldError != null ? messageSource.getMessage(fieldError, LocaleContextHolder.getLocale()) : messageSource.getMessage("validation.unknown", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(DemoDDApiResponse.fail(ErrorCode.INVALID_ARGUMENT.getCode(),
                field + ": " + message));
    }
}
