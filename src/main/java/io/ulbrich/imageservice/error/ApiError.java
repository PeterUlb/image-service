package io.ulbrich.imageservice.error;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ApiError {
    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String time;
    private final List<String> errors;

    public ApiError(Type type, List<String> errors) {
        this.status = type.status;
        this.code = type.code;
        this.message = type.message;
        this.errors = errors;
        this.time = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public ApiError(Type type, String error) {
        this(type, Collections.singletonList(error));
    }

    public ApiError(Type type) {
        this(type, Collections.emptyList());
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public List<String> getErrors() {
        return errors;
    }

    public enum Type {
        ARGUMENTS_INVALID("GEN-INV-ARG", HttpStatus.BAD_REQUEST, "Invalid Arguments"),
        RATE_LIMITED("GEN-RATE", HttpStatus.TOO_MANY_REQUESTS, "Rate Limited"),
        CAPTCHA_FAILED("CAPTCHA-FAIL", HttpStatus.BAD_REQUEST, "Captcha failed"),
        EXAMPLE_ERROR("GEN-GEN", HttpStatus.CONFLICT, "Example Error");

        private final String code;
        private final HttpStatus status;
        private final String message;

        Type(String code, HttpStatus status, String message) {
            this.code = code;
            this.status = status;
            this.message = message;
        }
    }
}
