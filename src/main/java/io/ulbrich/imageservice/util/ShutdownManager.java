package io.ulbrich.imageservice.util;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ShutdownManager {
    private final ApplicationContext appContext;

    ShutdownManager(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    public void initiateShutdown(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
        System.exit(returnCode);
    }
}
