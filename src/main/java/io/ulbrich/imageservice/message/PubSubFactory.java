package io.ulbrich.imageservice.message;

import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.stereotype.Component;
import org.threeten.bp.Duration;

import java.util.Optional;

@Component
public class PubSubFactory {
    private static final int MAX_INBOUND_MESSAGE_SIZE = 20 * 1024 * 1024; // 20MB API maximum message size.
    private final GcpProjectIdProvider gcpProjectIdProvider;
    private final CredentialsProvider credentialsProvider;

    @Value("${srv.gcp.pubsub-endpoint-override:#{null}}")
    private Optional<String> endpointOverride;

    public PubSubFactory(GcpProjectIdProvider gcpProjectIdProvider, CredentialsProvider credentialsProvider) {
        this.gcpProjectIdProvider = gcpProjectIdProvider;
        this.credentialsProvider = credentialsProvider;
    }

    public Subscriber createSubscriber(String subscriptionName, long queueSize, int poolSize, MessageReceiver receiver) {
        TransportChannelProvider channelProvider;

        // TODO: Instead, endpointOverride should always be passed. But if type = plaintext it should use this channelProvider
        if (endpointOverride.isPresent()) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(endpointOverride.get()).usePlaintext().build();
            channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        } else {
            channelProvider = SubscriptionAdminSettings.defaultGrpcTransportProviderBuilder()
                    .setMaxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                    .setKeepAliveTime(Duration.ofMinutes(5))
                    .build(); // Builder default
        }

        ProjectSubscriptionName projectSubscriptionName =
                ProjectSubscriptionName.of(gcpProjectIdProvider.getProjectId(), subscriptionName);

        FlowControlSettings flowControlSettings =
                FlowControlSettings.newBuilder()
                        .setMaxOutstandingElementCount(queueSize)
                        .build();

        ExecutorProvider executorProvider =
                InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(poolSize).build();

        Subscriber.Builder builder = Subscriber.newBuilder(projectSubscriptionName, receiver)
                .setCredentialsProvider(credentialsProvider)
                .setChannelProvider(channelProvider)
                .setFlowControlSettings(flowControlSettings)
                .setExecutorProvider(executorProvider);

        return builder.build();
    }
}
