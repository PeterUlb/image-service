package io.ulbrich.imageservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ulbrich.imageservice.error.ApiError;
import io.ulbrich.imageservice.service.RateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RateLimiterInterceptor implements HandlerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RateLimiterInterceptor.class);

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public RateLimiterInterceptor(RateLimitService rateLimitService, ObjectMapper objectMapper, MessageSource messageSource) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        var rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            return true;
        }

        String subject = request.getHeader("X-Forwarded-For");
        if (subject == null) {
            subject = request.getRemoteAddr();
        }
        subject = subject.split(",")[0];

        try {
            if (rateLimitService.isRateLimited(subject, rateLimited.group())) {
                var apiError = new ApiError.Builder(ApiError.Type.RATE_LIMITED, messageSource).args(new Object[]{rateLimited.group()}).build();
                response.setStatus(apiError.getStatus());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(apiError));
                response.getWriter().flush();
                return false;
            }
        } catch (JedisConnectionException e) {
            LOG.error("Redis could not be reached");
        }

        return true;
    }
}
