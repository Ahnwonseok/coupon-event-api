package com.sts.couponapi.service;

import com.sts.couponapi.dto.CouponEventDto;
import com.sts.couponapi.dto.CouponRegisterDto;
import com.sts.couponapi.dto.CouponResponseDto;
import com.sts.couponapi.entity.FinishEvent;
import com.sts.couponapi.repository.FinishEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WatingQueueService {

    private static String TOPIC_NAME = "coupon_event";
    private final RedisTemplate<String, Integer> redisTemplate;
    private final KafkaTemplate<Integer, Integer> kafkaTemplate;
    private final RedisTemplate<String, Object> registerCouponTemplate;
    private final FinishEventRepository finishEventRepository;

    @Transactional
    public Boolean setQueue(CouponEventDto dto) {
        ZSetOperations<String, Object> zSetOps = registerCouponTemplate.opsForZSet();
        Set<Object> members = zSetOps.range("A1001:1", 0, -1);
        ZSetOperations<String, Integer> waitingQueue = redisTemplate.opsForZSet();
        try {
            for (Object member : members) {
                Double tmpScore = zSetOps.score("A1001:1", member);
                String key = "A1001";
                int count = member.hashCode();
                Double score = tmpScore;
                if (count != 0) {
                    waitingQueue.add(key, count, score);
                    Set<Integer> datas = waitingQueue.range(key, 0, -1);
                    for (Integer data : datas) {
                        kafkaTemplate.send(TOPIC_NAME, data);
                        registerCouponTemplate.opsForValue().increment(dto.getCouponType(), -1);
                        datas.clear();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    @Transactional
    public String setCoupon(CouponRegisterDto dto) {
        ZSetOperations<String, Object> zSetOps = registerCouponTemplate.opsForZSet();
        boolean exists = registerCouponTemplate.hasKey(dto.getCouponType());
        if (exists) {
            return "이미 존재하는 쿠폰타입 입니다.";
        }
        boolean existsValue = registerCouponTemplate.opsForSet().isMember(dto.getCouponType(), dto.getDate());
        if (existsValue) {
            return "이미 존재하는 일정입니다.";
        }
        zSetOps.add(dto.getCouponType(),dto.getCount(),Double.parseDouble(dto.getDate()));
        return "쿠폰이 추가되었습니다.";
    }

    @Transactional
    public List<CouponResponseDto> getCoupon() {
        ZSetOperations<String, Object> zSetOps = registerCouponTemplate.opsForZSet();
        Set<Object> members = zSetOps.range("A1001:1", 0, -1);
        List<CouponResponseDto> couponList = new ArrayList<>();
        for (Object member: members) {
            CouponResponseDto couponDto = new CouponResponseDto();
            Double score = zSetOps.score("A1001:1", member);
            couponDto.setCouponType("A1001");
            couponDto.setDate(score);
            couponDto.setCount(member.hashCode());
            couponList.add(couponDto);
        }
        return couponList;
    }

    @Transactional
    public List<CouponResponseDto> finishEvent() {
        List<FinishEvent> events = finishEventRepository.findAll();
        List<CouponResponseDto> beforeList = new ArrayList<>();
        for (FinishEvent event : events) {
            CouponResponseDto coupon = new CouponResponseDto();
            coupon.setCouponType(event.getCouponType());
            coupon.setCount(event.getCount());
            coupon.setDate(event.getDate());
            beforeList.add(coupon);
        }
        return beforeList;
    }

}
