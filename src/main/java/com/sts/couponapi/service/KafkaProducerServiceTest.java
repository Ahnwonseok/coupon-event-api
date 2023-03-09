package com.sts.couponapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KafkaProducerServiceTest {

    private static String TOPIC_NAME = "kafkadam";

    private final KafkaTemplate<Integer, String> template;

    @Transactional
    public void publish() {
        for (int i=0;i<10;i++) {
            template.send(TOPIC_NAME,"test" + i);
        }
    }

    @Transactional
    @KafkaListener(topics = "kafkadam", groupId = "test-group-00")
    public void subscribe(String message) throws IOException {
        System.out.println(String.format("Consumed message : %s", message));
    }
}
