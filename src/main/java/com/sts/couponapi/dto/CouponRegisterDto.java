package com.sts.couponapi.dto;

import lombok.Getter;

@Getter
public class CouponRegisterDto {
    private String couponType;
    private String date;
    private int count;
}
