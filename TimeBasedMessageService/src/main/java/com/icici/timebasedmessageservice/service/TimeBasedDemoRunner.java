package com.icici.timebasedmessageservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TimeBasedDemoRunner implements ApplicationRunner {

    private final boolean runOnce;
    private final TimeBasedMessageService timeBasedMessageService;
    private final ApplicationContext applicationContext;

    public TimeBasedDemoRunner(
            @Value("${timebased.demo.run-once:false}") boolean runOnce,
            TimeBasedMessageServ ice timeBasedMessageService,
            ApplicationContext applicationContext) {
        this.runOnce = runOnce;
        this.timeBasedMessageService = timeBasedMessageService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!runOnce) {
            return;
        }
        timeBasedMessageService.processMessages();
        SpringApplication.exit(applicationContext, () -> 0);
    }
}
