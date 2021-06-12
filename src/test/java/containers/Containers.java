package containers;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public class Containers implements BeforeAllCallback, AfterAllCallback {
    private DockerComposeContainer<?> environment;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        environment = new DockerComposeContainer<>(new File("src/test/resources/compose-test.yml"))
                .withLocalCompose(true)
                .withExposedService("country-service-mock_1", 8080)
                .withExposedService("postgres_1", 5432)
                .withExposedService("redis_1", 6379);
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

        System.setProperty("srv.country-api.url", countryServiceUrl);
        System.setProperty("spring.datasource.url", postgresUrl);
        System.setProperty("srv.redis.url", redisUrl);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        environment.stop();
    }
}
