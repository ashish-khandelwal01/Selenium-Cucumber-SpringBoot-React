package com.framework.apiserver.controller;

import com.framework.apiserver.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KafkaController {

    @Autowired
    private KafkaProducer kafkaProducer;

    @GetMapping("/send")
    public String sendMessage() {
        String runId = "run-" + System.currentTimeMillis();
        kafkaProducer.sendHello(runId);
        return "Event sent: " + runId;
    }
}