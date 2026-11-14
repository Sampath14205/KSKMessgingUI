package com.ksk.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KskBusinessMessagingPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(KskBusinessMessagingPortalApplication.class, args);
    }
}
