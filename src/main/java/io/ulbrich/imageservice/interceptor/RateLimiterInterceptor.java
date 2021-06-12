package io.ulbrich.imageservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ulbrich.imageservice.error.ApiError;
import io.ulbrich.imageservice.service.RateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    public RateLimiterInterceptor(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            return true;
        }

        String subject = request.getHeader("X-FORWARDED-FOR");
        if (subject == null) {
            subject = request.getRemoteAddr();
        }

        try {
            if (rateLimitService.isRateLimited(subject, rateLimited.group())) {
                ApiError apiError = new ApiError(HttpStatus.TOO_MANY_REQUESTS, ApiError.Type.RATE_LIMITED, "Limit exceeded for " + rateLimited.group());

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
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
