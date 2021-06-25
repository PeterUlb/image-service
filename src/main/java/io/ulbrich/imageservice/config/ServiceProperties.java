package io.ulbrich.imageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "srv")
@Validated
public class ServiceProperties {
    @Valid
    private Redis redis;

    @Valid
    private Postgres pg;

    @Valid
    @NotNull
    private Upload upload;

    @Valid
    private CountryApi countryApi;

    @Valid
    private Gcp gcp;

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public Postgres getPg() {
        return pg;
    }

    public void setPg(Postgres pg) {
        this.pg = pg;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public CountryApi getCountryApi() {
        return countryApi;
    }

    public void setCountryApi(CountryApi countryApi) {
        this.countryApi = countryApi;
    }

    public Gcp getGcp() {
        return gcp;
    }

    public void setGcp(Gcp gcp) {
        this.gcp = gcp;
    }

    public static class Redis {
        @NotEmpty
        private String url;
        private int timeout = 300;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    public static class Postgres {
        @NotEmpty
        private String host;
        @NotNull
        private Integer port;
        @NotEmpty
        private String database;
        @NotEmpty
        private String username;
        @NotEmpty
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Upload {
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

    public static class CountryApi {
        @NotEmpty
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Gcp {
        @NotEmpty
        private String keyLocation = "/etc/secrets/srv/gcp/key.json";

        @NotEmpty
        private String projectId;

        private String storageHost;

        public String getKeyLocation() {
            return keyLocation;
        }

        public void setKeyLocation(String keyLocation) {
            this.keyLocation = keyLocation;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getStorageHost() {
            return storageHost;
        }

        public void setStorageHost(String storageHost) {
            this.storageHost = storageHost;
        }
    }
}
