package containers;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockContainerExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private static final AtomicBoolean started = new AtomicBoolean(false);
    public static String mockProjectId = "image-service-test";
    public static String mockTopic = "img-upload-test";
    public static String mockSubscription = "img-upload-test";
    private static DockerComposeContainer<?> environment;

    //Note: This is called beforeAll tests in a "test container", not once for all tests
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        if (started.getAndSet(true)) {
            return;
        }

        setup();
        // Register in root store to register for closing at the very end (after all tests) (Ryuk would also cleanup)
        extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put(this.getClass(), this);
    }

    private void setup() {
        environment = new DockerComposeContainer<>(new File("src/test/resources/compose-test.yml"))
                .withLocalCompose(true)
                .withServices("country-service-mock", "postgres-tmp", "redis", "fake-gcs-server", "gcp-pubsub-emulator")
                .withExposedService("country-service-mock_1", 8080)
                .withExposedService("postgres-tmp_1", 5432)
                .withExposedService("redis_1", 6379)
                .withExposedService("fake-gcs-server_1", 8080)
                .withExposedService("gcp-pubsub-emulator_1", 8080);
        environment.start();

        String countryServiceUrl = String.format("http://%s:%s",
                environment.getServiceHost("country-service-mock_1", 8080),
                environment.getServicePort("country-service-mock_1", 8080));
        String redisUrl = String.format("redis://%s:%s",
                environment.getServiceHost("redis_1", 6379),
                environment.getServicePort("redis_1", 6379));
        String gcsUrl = String.format("http://%s:%s",
                environment.getServiceHost("fake-gcs-server_1", 8080),
                environment.getServicePort("fake-gcs-server_1", 8080));
        String pubSubEndpoint = String.format("%s:%s",
                environment.getServiceHost("gcp-pubsub-emulator_1", 8080),
                environment.getServicePort("gcp-pubsub-emulator_1", 8080));

        initPubSub(pubSubEndpoint);

        System.setProperty("srv.country-api.url", countryServiceUrl);
        System.setProperty("srv.pg.host", environment.getServiceHost("postgres-tmp_1", 5432));
        System.setProperty("srv.pg.port", String.valueOf(environment.getServicePort("postgres-tmp_1", 5432)));
        System.setProperty("srv.pg.database", "img_srv_db");
        System.setProperty("srv.pg.username", "dev");
        System.setProperty("srv.pg.password", "letmein");
        System.setProperty("srv.gcp.project-id", mockProjectId);
        System.setProperty("srv.upload.bucket", "mock-bucket");
        System.setProperty("srv.upload.subscription-name", mockSubscription);
        System.setProperty("srv.redis.url", redisUrl);
        System.setProperty("srv.gcp.storage-host", gcsUrl);

        System.setProperty("spring.cloud.gcp.pubsub.emulator-host", pubSubEndpoint);
    }

    private void initPubSub(String pubSubEndpoint) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(pubSubEndpoint).usePlaintext().build();
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            NoCredentialsProvider credentialsProvider = NoCredentialsProvider.create();

            createTopic(mockTopic, channelProvider, credentialsProvider);
            createSubscription(mockSubscription, mockTopic, channelProvider, credentialsProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown();
        }
    }

    private void createTopic(String topicId, TransportChannelProvider channelProvider, NoCredentialsProvider credentialsProvider) throws IOException {
        TopicAdminSettings topicAdminSettings = TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
            TopicName topicName = TopicName.of(mockProjectId, topicId);
            topicAdminClient.createTopic(topicName);
        }
    }

    private void createSubscription(String subscriptionId, String topicId, TransportChannelProvider channelProvider, NoCredentialsProvider credentialsProvider) throws IOException {
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings);
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(mockProjectId, subscriptionId);
        subscriptionAdminClient.createSubscription(subscriptionName, TopicName.of(mockProjectId, topicId), PushConfig.getDefaultInstance(), 10);
    }

    @Override
    public void close() throws Throwable {
        if (environment != null) {
            environment.stop();
        }
    }
}
