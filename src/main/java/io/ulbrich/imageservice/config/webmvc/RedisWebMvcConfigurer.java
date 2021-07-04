package io.ulbrich.imageservice.config.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ulbrich.imageservice.interceptor.RateLimiterInterceptor;
import io.ulbrich.imageservice.service.RateLimitService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "srv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisWebMvcConfigurer implements WebMvcConfigurer {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public RedisWebMvcConfigurer(RateLimitService rateLimitService, ObjectMapper objectMapper, MessageSource messageSource) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimiterInterceptor(rateLimitService, objectMapper, messageSource));
    }
}
