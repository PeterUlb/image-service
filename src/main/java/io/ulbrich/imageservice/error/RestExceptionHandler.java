package io.ulbrich.imageservice.error;

import io.ulbrich.imageservice.exception.ExampleException;
import io.ulbrich.imageservice.exception.UnsupportedImageException;
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
    public ResponseEntity<ApiError> handleExampleException(ExampleException e) {
        var apiError = new ApiError.Builder(ApiError.Type.EXAMPLE_ERROR, messageSource).build();
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException e) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : e.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        var apiError = new ApiError.Builder(ApiError.Type.ARGUMENTS_INVALID, messageSource).errors(errors).build();
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(UnsupportedImageException.class)
    public ResponseEntity<ApiError> handleExampleException(UnsupportedImageException e) {
        var apiError = new ApiError.Builder(ApiError.Type.UNSUPPORTED_IMAGE, messageSource).args(new String[]{e.getDetectedType()}).build();
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }
}
