package com.sts.couponapi.controller;

import com.example.kafkademo.dto.CouponEventDto;
import com.example.kafkademo.dto.CouponRegisterDto;
import com.example.kafkademo.service.KafkaProducerServiceTest;
import com.example.kafkademo.service.WatingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaProducerTest {

    private final KafkaProducerServiceTest kafkaProducerServiceTest;
    private final WatingQueueService watingQueueService;

    @PostMapping("/producer")
    public ResponseEntity<?> producer() {
        kafkaProducerServiceTest.publish();
        return new ResponseEntity<>("success publish", HttpStatus.CREATED);
    }

    @PostMapping("/queue")
    public Boolean queue(@RequestBody CouponEventDto dto) {
        return watingQueueService.setQueue(dto);
    }

    @PostMapping("/coupon")
    public ResponseEntity<?> coupon(@RequestBody CouponRegisterDto dto) {
        watingQueueService.setCoupon(dto);
        return new ResponseEntity<>("success coupon register", HttpStatus.CREATED);
    }

}
