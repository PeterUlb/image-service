package io.ulbrich.imageservice.config.webmvc;

import io.ulbrich.imageservice.interceptor.CaptchaProtectedInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(prefix = "srv.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CaptchaWebMvcConfigurer implements WebMvcConfigurer {

    private final CaptchaProtectedInterceptor captchaProtectedInterceptor;

    public CaptchaWebMvcConfigurer(CaptchaProtectedInterceptor captchaProtectedInterceptor) {
        this.captchaProtectedInterceptor = captchaProtectedInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(captchaProtectedInterceptor);
    }
}
