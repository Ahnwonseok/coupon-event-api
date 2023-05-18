package com.sts.couponapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponRegisterDto {
    private String couponType;
    private Long date; //ex) 2305171650
    private int count;
}
