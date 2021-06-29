package io.ulbrich.imageservice.service;

import io.ulbrich.imageservice.client.CaptchaClient;
import io.ulbrich.imageservice.config.properties.CaptchaProperties;
import io.ulbrich.imageservice.dto.CaptchaResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "srv.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CaptchaServiceImpl implements CaptchaService {
    private static final Logger LOG = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    private final CaptchaProperties captchaProperties;
    private final CaptchaClient captchaClient;

    //TODO: Why is lazy need to break circular dependency... Without the feign client there's no issue, with it there's a circle for some reason
    public CaptchaServiceImpl(CaptchaProperties captchaProperties, @Lazy CaptchaClient captchaClient) {
        this.captchaProperties = captchaProperties;
        this.captchaClient = captchaClient;
    }

    @Override
    public boolean captchaSolved(String action, String userResponse, String remoteIp) {
        CaptchaResponseDto response = captchaClient.verify(captchaProperties.getSecretKey(), userResponse, remoteIp);
        if (response == null) {
            return false;
        }
        LOG.debug("{} {}", response.getScore(), response.getAction());
        return response.isSuccess() && response.getScore() > captchaProperties.getThreshold() && action.equals(response.getAction());
    }
}
