package io.ulbrich.imageservice.error;

import io.ulbrich.imageservice.exception.ExampleException;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {
    private final MessageSource messageSource;

    public RestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ExampleException.class)
    public ResponseEntity<ApiError> handleExampleException(ExampleException ex) {
        var apiError = new ApiError.Builder(ApiError.Type.EXAMPLE_ERROR, messageSource).build();
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        var apiError = new ApiError.Builder(ApiError.Type.ARGUMENTS_INVALID, messageSource).errors(errors).build();
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }
}
