package day.ohya.demodd.exception;

import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.model.DemoDDApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<DemoDDApiResponse<Void>> handleBusiness(BusinessException ex, Locale locale) {
        String message = messageSource.getMessage(
                ex.getError().getMessageKey(), ex.getArgs(), locale);
        return ResponseEntity.ok(DemoDDApiResponse.fail(ex.getError(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DemoDDApiResponse<Void>> handleOther(Exception ex) {
        log.error("Unknown Exception", ex);
        return ResponseEntity.ok(DemoDDApiResponse.fail(ErrorCode.SYSTEM_ERROR, ex.getMessage()));
    }
}