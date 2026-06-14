package opencode.examples.springboot.controller;

import lombok.extern.slf4j.Slf4j;
import opencode.sdk.invoker.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        HttpStatus status = resolveHttpStatus(ex.getCode());
        log.warn("ApiException: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    private HttpStatus resolveHttpStatus(int code) {
        if (code < 100 || code > 599) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        HttpStatus resolved = HttpStatus.resolve(code);
        return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
