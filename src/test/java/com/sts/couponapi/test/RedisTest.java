package com.sts.couponapi.test;

import com.sts.couponapi.members.entity.Members;
import com.sts.couponapi.members.entity.MembersRoleEnum;
import com.sts.couponapi.service.WatingQueueService;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class RedisTest {

    @Autowired
    private WatingQueueService watingQueueService;

    @Autowired
    RedisTemplate<String, Long> redisTemplate;

    @Test
    void 선착순이벤트() throws InterruptedException {

        int threadCount = 50;

        ExecutorService executorService = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(
                    () ->  {
                        try {
                            Members member = new Members("user"+ finalI,"1234", MembersRoleEnum.MEMBER);
                            watingQueueService.setQueue(member);

                        } finally {
                            latch.countDown();
                        }
                    }
            );
        }
        latch.await();

        //score = A1001 쿠폰의 개수
        ZSetOperations<String, Long> coupons = redisTemplate.opsForZSet();
        Double score = coupons.score("A1001",Long.parseLong("2305211618"));

        Assertions.assertEquals(score,0);
    }
}
