package com.sts.couponapi.controller;

import com.sts.couponapi.dto.CouponEventDto;
import com.sts.couponapi.dto.CouponRegisterDto;
import com.sts.couponapi.dto.CouponResponseDto;
import com.sts.couponapi.security.UserDetailsImpl;
import com.sts.couponapi.service.WatingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponEventController {
    private final WatingQueueService watingQueueService;

    //쿠폰 신청하기
    @PostMapping("/queue")
    public void queue(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        watingQueueService.setQueue(userDetails.getMember());
    }

    @PostMapping("/coupon")
    public ResponseEntity<?> coupon(@RequestBody CouponRegisterDto dto) {
        String data = watingQueueService.setCoupon(dto);
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    @GetMapping("/coupon")
    public List<CouponResponseDto> getCoupon() {
        return watingQueueService.getCoupon();
    }

    @GetMapping("/finishEvent")
    public List<CouponResponseDto> finishEvent() {
        return watingQueueService.finishEvent();
    }

    @GetMapping("/main")
    public String main() {
        return "스케쥴링 수정!";
    }

}
