package com.sts.couponapi.service;

import com.sts.couponapi.dto.CouponEventDto;
import com.sts.couponapi.dto.CouponRegisterDto;
import com.sts.couponapi.dto.CouponResponseDto;
import com.sts.couponapi.entity.FinishEvent;
import com.sts.couponapi.members.entity.Members;
import com.sts.couponapi.repository.FinishEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.Redis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatingQueueService {

    private final RedisTemplate<String, Long> registerCouponTemplate;
    private final FinishEventRepository finishEventRepository;
    private final RedisTemplate<String,String> userQueue;

    //이벤트 참여하기
    @Transactional
    public void setQueue(Members members) {
        String userName = members.getUsername();
        //당첨 유저의 sorted set
        ZSetOperations<String, String> queue = userQueue.opsForZSet();
        Boolean memberExists = userQueue.hasKey(userName);

        if (memberExists)
            log.info("이미 이벤트에 참여했습니다.");

        ZSetOperations<String, Long> coupons = registerCouponTemplate.opsForZSet();
        Double couponNum = coupons.score("A1001",Long.parseLong("2305211618"));

        //쿠폰 갯수가 다 떨어질 경우
        if (couponNum == 0)
            log.info ("선착순 이벤트가 종료되었습니다.");

        Set<Long> couponList = coupons.range("A1001", 0, -1);
        for (Long coupon : couponList) {
            if (coupons.score("A1001",coupon) > 0) {
                //당첨자 sorted set에서 따로 저장
                queue.add("WINNER", userName, System.currentTimeMillis());

                //당첨자가 나올 경우 쿠폰개수 하나씩 감소
                coupons.incrementScore("A1001",coupon,-1);
                log.info ("당첨! 마이페이지를 확인해주세요");
            }
        }
    }

    //쿠폰 만들기
    @Transactional
    public String setCoupon(CouponRegisterDto dto) {
        ZSetOperations<String, Long> zSetOps = registerCouponTemplate.opsForZSet();
        boolean exists = registerCouponTemplate.hasKey(dto.getCouponType());
        if (exists) {
            return "이미 존재하는 쿠폰타입 입니다.";
        }
        boolean existsValue = registerCouponTemplate.opsForSet().isMember(dto.getCouponType(), dto.getDate());
        if (existsValue) {
            return "이미 존재하는 일정입니다.";
        }
        zSetOps.add(dto.getCouponType(),dto.getDate(),dto.getCount());
        return "쿠폰이 추가되었습니다.";
    }

    @Transactional
    public List<CouponResponseDto> getCoupon() {
        ZSetOperations<String, Long> zSetOps = registerCouponTemplate.opsForZSet();
        Set<Long> members = zSetOps.range("A1001", 0, -1);
        List<CouponResponseDto> couponList = new ArrayList<>();
        for (Long member: members) {
            CouponResponseDto couponDto = new CouponResponseDto();
            double score = zSetOps.score("A1001", member);
            couponDto.setCouponType("A1001");
            couponDto.setCount((long)score);
            couponDto.setDate(member);
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
