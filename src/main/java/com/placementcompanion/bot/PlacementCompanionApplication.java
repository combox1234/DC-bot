package com.placementcompanion.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlacementCompanionApplication {

    private static org.springframework.context.ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(PlacementCompanionApplication.class, args);
    }

    public static org.springframework.context.ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
