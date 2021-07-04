package io.ulbrich.imageservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ulbrich.imageservice.error.ApiError;
import io.ulbrich.imageservice.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "srv.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CaptchaProtectedInterceptor implements HandlerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(CaptchaProtectedInterceptor.class);

    private final CaptchaService captchaService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public CaptchaProtectedInterceptor(CaptchaService captchaService, ObjectMapper objectMapper, MessageSource messageSource) {
        this.captchaService = captchaService;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        var captchaAnnotation = handlerMethod.getMethodAnnotation(CaptchaProtected.class);
        if (captchaAnnotation == null) {
            return true;
        }

        String captchaResponse = request.getHeader("captcha-response");
        if (captchaResponse == null) {
            return getInvalidCaptchaResponse(response);
        }

        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) {
            remoteIp = request.getRemoteAddr();
        }
        remoteIp = remoteIp.split(",")[0];

        if (!captchaService.captchaSolved(captchaAnnotation.action(), captchaResponse, remoteIp)) {
            return getInvalidCaptchaResponse(response);
        }

        return true;
    }

    private boolean getInvalidCaptchaResponse(@NonNull HttpServletResponse response) throws IOException {
        var apiError = new ApiError.Builder(ApiError.Type.CAPTCHA_FAILED, messageSource).build();
        response.setStatus(apiError.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
        response.getWriter().flush();
        return false;
    }
}
