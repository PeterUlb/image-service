package io.ulbrich.imageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "srv.upload")
@Validated
public class UploadProperties {
    @NotEmpty
    private String bucket;
    @NotEmpty
    private String subscriptionName;
    @NotNull
    private Integer poolSize;
    @NotNull
    private Long queueSize;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Long getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Long queueSize) {
        this.queueSize = queueSize;
    }
}
