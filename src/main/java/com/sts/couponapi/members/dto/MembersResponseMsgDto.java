package com.sts.couponapi.members.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MembersResponseMsgDto {
    private String msg;
    private int httpStatus;

    public MembersResponseMsgDto(String msg, int httpStatus) {
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

}
