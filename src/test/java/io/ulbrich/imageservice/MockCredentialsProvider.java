package io.ulbrich.imageservice;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.ServiceAccountSigner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class MockCredentialsProvider implements CredentialsProvider {
    @Override
    public Credentials getCredentials() throws IOException {
        return new TestGcpCredentials();
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

        }

        @Override
        public String getAccount() {
            return "MOCK-ACCOUNT";
        }

        @Override
        public byte[] sign(byte[] toSign) {
            byte[] b = new byte[1024];
            new Random().nextBytes(b);
            return b;
        }
    }
}
