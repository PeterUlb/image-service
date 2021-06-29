package io.ulbrich.imageservice.config.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "srv.captcha")
@ConditionalOnProperty(prefix = "srv.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
@Validated
public class CaptchaProperties {
    // See spring-boot/issues/2282
    private boolean enabled;
    @NotEmpty
    private String siteKey;
    @NotEmpty
    private String secretKey;
    @NotNull
    private Float threshold;
    @NotNull
    private String url;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Float getThreshold() {
        return threshold;
    }

    public void setThreshold(Float threshold) {
        this.threshold = threshold;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
