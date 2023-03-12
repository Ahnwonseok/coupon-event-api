package com.sts.couponapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponRegisterDto {
    private String couponType;
    private double date;
    private int count;
}
