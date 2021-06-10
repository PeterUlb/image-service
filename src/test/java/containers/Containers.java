package containers;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public class Containers implements BeforeAllCallback, AfterAllCallback {
    private DockerComposeContainer<?> environment;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        environment = new DockerComposeContainer<>(new File("src/test/resources/compose-test.yml"))
                .withLocalCompose(true)
                .withExposedService("country-service-mock_1", 8080)
                .withExposedService("postgres_1", 5432);
        environment.start();

        String countryServiceUrl = String.format("http://%s:%s",
                environment.getServiceHost("country-service-mock_1", 8080),
                environment.getServicePort("country-service-mock_1", 8080));
        String postgresUrl = String.format("jdbc:postgresql://%s:%s/imageservicedb",
                environment.getServiceHost("postgres_1", 5432),
                environment.getServicePort("postgres_1", 5432));

        System.setProperty("country-api.url", countryServiceUrl);
        System.setProperty("spring.datasource.url", postgresUrl);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        environment.stop();
    }
}
