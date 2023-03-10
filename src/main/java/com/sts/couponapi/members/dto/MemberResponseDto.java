package com.sts.couponapi.members.dto;

import com.sts.couponapi.members.entity.MembersRoleEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseDto {

    private String address;
    private String username;
    private String email;
    private String nickname;
    private MembersRoleEnum role;
}
