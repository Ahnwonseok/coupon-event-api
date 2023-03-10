package com.sts.couponapi.members.entity;

public enum MembersRoleEnum {
    //enum 상수 정의
    //member, admin은 Authority의 member, admin 값을 가짐
    MEMBER(Authority.MEMBER),
    ADMIN(Authority.ADMIN);

    //memberRoleEnum 객체 생성할 때 authority 할당
    //객체 생성 이후에는 값을 변경할 수 없도록 private, final 선언
    private final String authority;

    //생성자
    //객체 생성할 때 무조건 authority를 줘야함
    MembersRoleEnum(String authority) {
        this.authority = authority;
    }

    //객체의 authority 가져오기
    public String getAuthority() {
        return this.authority;
    }

    //멤버의 권한을 authority에서 가져가서 할당하도록 하였음.
    //그냥 MEMBER("ROLE_MEMBER")이렇게 써도 되긴함.
    //다만 권한을 적용시킬 때, 오타를 방지하고자 아래처럼 사용하였음.
    public static class Authority {
        public static final String MEMBER = "ROLE_MEMBER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
