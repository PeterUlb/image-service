package io.ulbrich.imageservice.mocks;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.ulbrich.imageservice.config.ServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("test")
public class MockConfiguration {
    private final ServiceProperties serviceProperties;
    private final CredentialsProvider credentialsProvider;

    public MockConfiguration(ServiceProperties serviceProperties, CredentialsProvider credentialsProvider) {
        this.serviceProperties = serviceProperties;
        this.credentialsProvider = credentialsProvider;
    }

    @Bean
    @Primary
    public Storage storage() throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(serviceProperties.getGcp().getProjectId())
                .setHost(serviceProperties.getGcp().getStorageHost())
                .setCredentials(credentialsProvider.getCredentials())
                .build()
                .getService();
    }
}
