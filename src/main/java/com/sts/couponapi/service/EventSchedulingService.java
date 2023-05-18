package com.sts.couponapi.service;

import com.sts.couponapi.dto.CouponResponseDto;
import com.sts.couponapi.entity.FinishEvent;
import com.sts.couponapi.repository.FinishEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSchedulingService {

    private final RedisTemplate<String, Long> registerCouponTemplate;
    private final FinishEventRepository finishEventRepository;
    private static String EVENT_NAME = "A1001";
    
    @Scheduled(fixedDelay = 1000) //1초마다 실행
    public void moveToMySQL() {
        //쿠폰의 sorted set
        ZSetOperations<String, Long> zSetOps = registerCouponTemplate.opsForZSet();
        Set<Long> coupons = zSetOps.range(EVENT_NAME, 0, -1);
        LocalDateTime now = LocalDateTime.now();

        // 년/월/일/시/분을 2자리 문자열로 표시
        String year = String.format("%02d", now.getYear() % 100);
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String hour = String.format("%02d", now.getHour());
        String minute = String.format("%02d", now.getMinute());

        //현재 시각 (2305171650)
        String nowTime = year + month + day + hour + minute;

        for (Long coupon : coupons) {

            //해당 쿠폰의 개수(score)
            double couponNum = zSetOps.score(EVENT_NAME, coupon);

            //선착순 이벤트 종료시간(2305171650)
            long endTime = coupon.longValue();

            //만약 이벤트 종료시간이 된다면
            if (Long.parseLong(nowTime) >= endTime) {
                //종료된 이벤트를 저장
                FinishEvent event = new FinishEvent();
                event.setCouponType(EVENT_NAME);
                event.setDate(coupon);
                event.setCount((long)couponNum);
                finishEventRepository.save(event);

                //종료된 쿠폰을 삭제
                zSetOps.remove(EVENT_NAME, coupon);
                //이벤트 종료
                return;
            }

        }
    }
}