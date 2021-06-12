package io.ulbrich.imageservice.error;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

public class ApiError {
    private final HttpStatus status;
    private final long code;
    private final String message;
    private final List<String> errors;

    public ApiError(HttpStatus status, Type type, List<String> errors) {
        this.status = status;
        this.code = type.code;
        this.message = type.message;
        this.errors = errors;
    }

    public ApiError(HttpStatus status, Type type, String error) {
        this.status = status;
        this.code = type.code;
        this.message = type.message;
        this.errors = Collections.singletonList(error);
    }

    public ApiError(HttpStatus status, Type type) {
        this.status = status;
        this.code = type.code;
        this.message = type.message;
        this.errors = null;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public enum Type {
        ARGUMENTS_INVALID(3, "Invalid Arguments"), RATE_LIMITED(50, "Rate Limited"), EXAMPLE_ERROR(33, "Example Error");

        private final long code;
        private final String message;

        Type(long code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
