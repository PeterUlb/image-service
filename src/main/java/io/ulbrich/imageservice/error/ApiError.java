package io.ulbrich.imageservice.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ApiError {
    private static final Logger LOG = LoggerFactory.getLogger(ApiError.class);

    private final int status;
    private final String code;
    private final String message;
    private final String time;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<String> errors;

    private ApiError(Builder builder) {
        this.status = builder.type.status.value();
        this.code = builder.type.code;
        this.message = builder.messageSource.getMessage(builder.type.code, builder.args, LocaleContextHolder.getLocale());
        this.time = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
        this.errors = builder.errors;
        LOG.debug("Responding in locale {}", LocaleContextHolder.getLocale());
    }

    public int getStatus() {
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
        ARGUMENTS_INVALID("GEN-INV-ARG", HttpStatus.BAD_REQUEST),
        RATE_LIMITED("GEN-RATE", HttpStatus.TOO_MANY_REQUESTS),
        CAPTCHA_FAILED("CAPTCHA-FAIL", HttpStatus.BAD_REQUEST),
        EXAMPLE_ERROR("GEN-GEN", HttpStatus.CONFLICT);

        private final String code;
        private final HttpStatus status;

        Type(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }
    }

    public static class Builder {
        private final Type type;
        private final MessageSource messageSource;
        private Object[] args;
        private List<String> errors;

        public Builder(Type type, MessageSource messageSource) {
            this.type = type;
            this.messageSource = messageSource;
        }

        public Builder args(Object[] args) {
            this.args = args;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public ApiError build() {
            return new ApiError(this);
        }
    }
}
