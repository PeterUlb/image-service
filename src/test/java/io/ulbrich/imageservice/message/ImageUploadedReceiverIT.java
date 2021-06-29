package io.ulbrich.imageservice.message;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import containers.MockContainerExtension;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.ulbrich.imageservice.model.Image;
import io.ulbrich.imageservice.model.ImagePrivacy;
import io.ulbrich.imageservice.model.ImageStatus;
import io.ulbrich.imageservice.repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ExtendWith(MockContainerExtension.class)
@ActiveProfiles("test")
class ImageUploadedReceiverIT {

    @Value("${spring.cloud.gcp.pubsub.emulator-host:#{null}}")
    private String endpointOverride;

    @Autowired
    private CredentialsProvider credentialsProvider;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    void testPubSubConfirm() throws Exception {
        imageRepository.save(Image.withInitialState("BVXErLFEgv", UUID.randomUUID(), "mock", "mock",
                "mock.png", 200L, ImagePrivacy.PUBLIC, Collections.emptySet()));
        imageRepository.flush();

        ManagedChannel channel = ManagedChannelBuilder.forTarget(endpointOverride).usePlaintext().build();
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

            Publisher publisher =
                    Publisher.newBuilder(TopicName.newBuilder().setProject(MockContainerExtension.mockProjectId).setTopic(MockContainerExtension.mockTopic).build())
                            .setChannelProvider(channelProvider)
                            .setCredentialsProvider(credentialsProvider)
                            .build();
            String msg = "{\"name\":\"images/BVXErLFEgv\",\"bucket\":\"some-bucket\"}";
            PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(msg)).build();
            ApiFuture<String> apiFuture = publisher.publish(message);
            apiFuture.get();
        } finally {
            channel.shutdown();
        }

        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            var imageStatus = imageRepository.findByExternalKey("BVXErLFEgv").orElseThrow().getImageStatus();
            return imageStatus.equals(ImageStatus.VERIFIED);
        });
        var image = imageRepository.findByExternalKey("BVXErLFEgv").orElse(null);
        assertThat(image).isNotNull();
        assertThat(image.getImageStatus()).isEqualTo(ImageStatus.VERIFIED);
    }
}