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
import containers.Containers;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.ulbrich.imageservice.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ExtendWith(Containers.class)
@ActiveProfiles("test")
class ImageUploadedReceiverIT {

    @Value("${spring.cloud.gcp.pubsub.emulator-host:#{null}}")
    private String endpointOverride;

    @Autowired
    private CredentialsProvider credentialsProvider;

    @MockBean
    private ImageService imageService;

    @Test
    void testPubSubConfirm() throws Exception {
        Mockito.doNothing().when(imageService).processImageAfterUpload(Mockito.isA(String.class));

        ManagedChannel channel = ManagedChannelBuilder.forTarget(endpointOverride).usePlaintext().build();
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

            Publisher publisher =
                    Publisher.newBuilder(TopicName.newBuilder().setProject("image-service-test").setTopic("img-upload-test").build())
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

        Mockito.verify(imageService, Mockito.timeout(1000).atLeastOnce()).processImageAfterUpload(Mockito.any());
    }
}