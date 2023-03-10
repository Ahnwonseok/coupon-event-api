package com.sts.couponapi.security;

import com.sts.couponapi.members.entity.Members;
import com.sts.couponapi.members.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor

/*
  spring security가 제공하는 인증 관련 기능을 구현하는데 필요한 메서드를 제공하는 클래스
  userDetailsService를 구현하여 사용자의 정보를 가져오고 인증, 권한을 검사한다.
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    private final MembersRepository membersRepository;

    /**
     * username을 전달받아 이것을 토대로 해당되는 사용자가 있는지를 검사함.
     * @param username 클라이언트로부터 전달받은 username
     * @throws UsernameNotFoundException
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername = {} ", username);

        Members members = membersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new UserDetailsImpl(members, members.getUsername(), members.getPassword());
    }
}
