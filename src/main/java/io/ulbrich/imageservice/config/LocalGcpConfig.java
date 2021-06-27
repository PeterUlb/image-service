package io.ulbrich.imageservice.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.ServiceAccountSigner;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Configuration
@Profile({"local-gcp", "test"})
public class LocalGcpConfig {
    @Bean
    public Storage storage(CredentialsProvider credentialsProvider, GcpProjectIdProvider gcpProjectIdProvider, ServiceProperties serviceProperties) throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(gcpProjectIdProvider.getProjectId())
                .setHost(serviceProperties.getGcp().getStorageHost())
                .setCredentials(credentialsProvider.getCredentials())
                .build()
                .getService();
    }

    @Bean
    public CredentialsProvider credentialsProvider() {
        return TestGcpCredentials::new;
    }

    @Bean
    public GcpProjectIdProvider gcpProjectIdProvider(ServiceProperties serviceProperties) {
        return () -> serviceProperties.getGcp().getProjectId();
    }

    /**
     * Credentials is needed for the Emulator (injected into multiple places, replaces the DefaultCredentialsProvider)
     * Service Account Signer is needed so the signed URL function returns a value
     */
    static class TestGcpCredentials extends Credentials implements ServiceAccountSigner {
        @Override
        public String getAuthenticationType() {
            return null;
        }

        @Override
        public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
            return null;
        }

        @Override
        public boolean hasRequestMetadata() {
            return false;
        }

        @Override
        public boolean hasRequestMetadataOnly() {
            return false;
        }

        @Override
        public void refresh() throws IOException {
            // Not needed
        }

        @Override
        public String getAccount() {
            return "MOCK-ACCOUNT";
        }

        @Override
        public byte[] sign(byte[] toSign) {
            var b = new byte[1024];
            new Random().nextBytes(b);
            return b;
        }
    }
}
