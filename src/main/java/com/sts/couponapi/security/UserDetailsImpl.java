package com.sts.couponapi.security;

import com.sts.couponapi.members.entity.Members;
import com.sts.couponapi.members.entity.MembersRoleEnum;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

//spring security에서 사용자 계정 정보를 저장하는데 쓰임.
public class UserDetailsImpl implements UserDetails {

    private final Members members;

    private final String username;
    private final String password;

    //생성자
    @Builder
    public UserDetailsImpl(Members members, String username, String password) {
        this.members = members;
        this.username = username;
        this.password = password;
    }

    public Members getMember() {
        return members;
    }

    /*
    userdetails 구현
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //현재 member 객체에서 권한을 가져와 role에 할당
        MembersRoleEnum role = members.getRole();
        //권한을 나타내는 문자열을 authority에 넣어줌
        String authority = role.getAuthority();

        //authority값을 가지는 simpleGrantedAuthority 생성
        //simpleGrantedAuthority는 객체의 권한을 표현하는 클래스
        //출처 : https://docs.spring.io/spring-security/site/docs/4.0.x/apidocs/org/springframework/security/core/authority/SimpleGrantedAuthority.html
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);

        //반환타입에 맞춰서 리턴해줘야 하니까 리스트 생성
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        //권한 목록에 추가
        authorities.add(simpleGrantedAuthority);

        return authorities;
    }


    @Override
    public String getUsername() {
        return this.username;
    }

    //계정에 대한 비밀번호를 반환하지 않음
    @Override
    public String getPassword() {
        return null;
    }

    //계정이 만료되었는지
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    //계정이 잠겨있는지
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    //자격 증명이 만료되었는지
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    //계정이 활성화 되었는지
    @Override
    public boolean isEnabled() {
        return false;
    }
}