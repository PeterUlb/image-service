package io.ulbrich.imageservice.config.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ulbrich.imageservice.interceptor.RateLimiterInterceptor;
import io.ulbrich.imageservice.service.RateLimitService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "srv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisWebMvcConfigurer implements WebMvcConfigurer {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RedisWebMvcConfigurer(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimiterInterceptor(rateLimitService, objectMapper));
    }
}
