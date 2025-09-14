package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestRunEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, TestRunEvent> kafkaTemplate;

    public void sendHello(String runId) {
        TestRunEvent event = new TestRunEvent(runId, "REQUESTED");
        kafkaTemplate.send("spring-boot-cucumber-topic", event);
        System.out.println("Sent event: " + runId);
    }
}