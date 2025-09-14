package com.framework.apiserver.service;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "spring-boot-cucumber-topic", groupId = "automation-group")
public class KafkaConsumer {
    @KafkaHandler
    public void listen(String message) {
        System.out.println("Received: " + message);
    }
}
