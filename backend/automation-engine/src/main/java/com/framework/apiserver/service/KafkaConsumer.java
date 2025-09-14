package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestRunEvent;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "spring-boot-cucumber-topic", groupId = "automation-group")
public class KafkaConsumer {
    @KafkaHandler
    public void listen(TestRunEvent event) throws InterruptedException {
        System.out.println("Starting fake test run: " + event.getRunId());
        Thread.sleep(1000); // simulate async execution
        System.out.println("Completed fake test run: " + event.getRunId());
    }
}
