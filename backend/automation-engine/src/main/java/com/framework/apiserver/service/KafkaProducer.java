package com.framework.apiserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendHello() {
        kafkaTemplate.send("spring-boot-cucumber-topic", "Hello Ashish Kafka!");
    }
}