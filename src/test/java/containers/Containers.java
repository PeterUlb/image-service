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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;

public class Containers implements BeforeAllCallback, AfterAllCallback {
    private DockerComposeContainer<?> environment;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        environment = new DockerComposeContainer<>(new File("src/test/resources/compose-test.yml"))
                .withLocalCompose(true)
                .withExposedService("country-service-mock_1", 8080)
                .withExposedService("postgres_1", 5432)
                .withExposedService("redis_1", 6379)
                .withExposedService("fake-gcs-server_1", 8080)
                .withExposedService("gcp-pubsub-emulator_1", 8080);
        environment.start();

        String countryServiceUrl = String.format("http://%s:%s",
                environment.getServiceHost("country-service-mock_1", 8080),
                environment.getServicePort("country-service-mock_1", 8080));
        String postgresUrl = String.format("jdbc:postgresql://%s:%s/imageservicedb",
                environment.getServiceHost("postgres_1", 5432),
                environment.getServicePort("postgres_1", 5432));
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
        System.setProperty("spring.datasource.url", postgresUrl);
        System.setProperty("srv.redis.url", redisUrl);
        System.setProperty("spring.cloud.gcp.project-id", "image-service-test");
        System.setProperty("spring.cloud.gcp.datastore.host", gcsUrl);
        System.setProperty("spring.cloud.gcp.pubsub.emulator-host", pubSubEndpoint);
        System.setProperty("srv.upload.subscription-name", "img-upload-test-sub");
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        environment.stop();
    }

    private void initPubSub(String pubSubEndpoint) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(pubSubEndpoint).usePlaintext().build();
        String subscriptionId = "img-upload-test-sub";
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            NoCredentialsProvider credentialsProvider = NoCredentialsProvider.create();

            String topicId = "img-upload-test";
            createTopic(topicId, channelProvider, credentialsProvider);

            createSubscription(subscriptionId, topicId, channelProvider, credentialsProvider);
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
            TopicName topicName = TopicName.of("image-service-test", topicId);
            topicAdminClient.createTopic(topicName);
        }
    }

    private void createSubscription(String subscriptionId, String topicId, TransportChannelProvider channelProvider, NoCredentialsProvider credentialsProvider) throws IOException {
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings);
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("image-service-test", subscriptionId);
        subscriptionAdminClient.createSubscription(subscriptionName, TopicName.of("image-service-test", topicId), PushConfig.getDefaultInstance(), 10);
    }
}
