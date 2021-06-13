package io.ulbrich.imageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.ulbrich.imageservice.config.UploadProperties;
import io.ulbrich.imageservice.service.ImageService;
import io.ulbrich.imageservice.util.ShutdownManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ImageUploadedReceiver implements MessageReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(ImageUploadedReceiver.class);

    private final PubSubFactory pubSubFactory;
    private final UploadProperties uploadProperties;
    private final ImageService imageService;
    private final ShutdownManager shutdownManager;
    private final SubscriptionAdminClient subscriptionAdminClient;
    private final GcpProjectIdProvider gcpProjectIdProvider;

    Subscriber subscriber;

    public ImageUploadedReceiver(PubSubFactory pubSubFactory, UploadProperties uploadProperties, ImageService imageService, ShutdownManager shutdownManager, SubscriptionAdminClient subscriptionAdminClient, GcpProjectIdProvider gcpProjectIdProvider) {
        this.pubSubFactory = pubSubFactory;
        this.uploadProperties = uploadProperties;
        this.imageService = imageService;
        this.shutdownManager = shutdownManager;
        this.subscriptionAdminClient = subscriptionAdminClient;
        this.gcpProjectIdProvider = gcpProjectIdProvider;
    }

    @PostConstruct
    public void onStart() {
        LOG.debug("Starting Receiver");

        try (subscriptionAdminClient) {
            var subscriptionName = ProjectSubscriptionName.of(gcpProjectIdProvider.getProjectId(), uploadProperties.getSubscriptionName());
            subscriptionAdminClient.getSubscription(subscriptionName); //NodeJS has exists which calls metadata, java doesn't have this :/
            //This needs Pub/Sub Viewer role
        } catch (Exception e) {
            LOG.error("Error with Subscription " + uploadProperties.getSubscriptionName() + "(" + gcpProjectIdProvider.getProjectId() + ")", e);
            shutdownManager.initiateShutdown(-1);
            return;
        }

        subscriber = pubSubFactory.createSubscriber(uploadProperties.getSubscriptionName(),
                uploadProperties.getQueueSize(), uploadProperties.getPoolSize(), this);
        subscriber.startAsync().awaitRunning();
    }

    @PreDestroy
    public void onStop() {
        LOG.warn("Stopping Receiver");
        subscriber.stopAsync();
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message.getData().toStringUtf8());
        }
        JsonNode json;
        try {
            json = new ObjectMapper().readTree(message.getData().toStringUtf8());
            String key = json.at("/name").asText();
            LOG.debug(key);
            imageService.processImageAfterUpload(key);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            consumer.ack();
        }
    }
}
