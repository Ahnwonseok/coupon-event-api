package com.sts.couponapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponResponseDto {
    private String couponType;
    private Long date;
    private Long count;
}
