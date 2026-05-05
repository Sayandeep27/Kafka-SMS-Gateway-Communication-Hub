package com.icici.timebasedmessageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeBasedMessageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeBasedMessageServiceApplication.class, args);
    }
}
