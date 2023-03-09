package com.sts.couponapi.service;

import com.sts.couponapi.dto.CouponEventDto;
import com.sts.couponapi.dto.CouponRegisterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WatingQueueService {

    private static String TOPIC_NAME = "coupon_event";
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final RedisTemplate<String, Integer> registerCouponTemplate;

    @Transactional
    public Boolean setQueue(CouponEventDto dto) {
        ValueOperations<String, Integer> couponCount = registerCouponTemplate.opsForValue();
        if (couponCount.get(dto.getCouponType()).equals(0))
            return false;
        String key = dto.getCouponType().split(":")[0];
        String value = dto.getUsername();
        ZSetOperations<String, String> waitingQueue = redisTemplate.opsForZSet();
        if (waitingQueue.zCard(key) != 0 && waitingQueue.rank(key, value) != null) {
            return false;
        }
        waitingQueue.add(key, value, (double)System.currentTimeMillis());
        if (waitingQueue.zCard(key) >= 100) {
            Set<String> datas = waitingQueue.range(key, 0, -1);
            for (String data : datas) {
                kafkaTemplate.send(TOPIC_NAME, data);
                registerCouponTemplate.opsForValue().increment(dto.getCouponType(), -1);
            }
            waitingQueue.removeRange(key, 0, -1);
        }
        return true;
    }

    @Transactional
    public boolean testQueue(CouponEventDto dto) {
        String key = dto.getCouponType().toString();
        String value = dto.getUsername().toString();
        double score = 1;
        ZSetOperations<String, String> waitingQueue = redisTemplate.opsForZSet();
        waitingQueue.add(key, value, score);
        return true;
    }

    @Transactional
    public void setCoupon(CouponRegisterDto dto) {
        registerCouponTemplate.opsForValue().set(dto.getCouponType(), dto.getCount());
    }
}
