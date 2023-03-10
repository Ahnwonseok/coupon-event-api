package com.sts.couponapi.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sts.couponapi.security.dto.SecurityExceptionDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    //스프링 시큐리티 필터 구현

    //filterChain은!
    //필터는 애플리케이션의 요청 및 응답을 처리하는데 사용됨.
    //각 요청은 특정한 순서로 체인을 통과함.
    //그래서 doFilter의 마지막 리소스에 도달할 때 까지 체인의 메서드가 차례로 실행됨.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //들어오는 요청에서 토큰을 추출함
        String token = jwtUtil.resolveToken(request);
//        log.info("resolved token = {}",token);

        //토큰이 null이 아니면 jwtUtil.validateToken(token)을 통해 유효성을 검사함.
        //토큰이 유효하지 않으면 "토큰이 유효하지 않습니다."라는 메시지와 함께 오류 응답을 클라이언트에 보냄. ->
        //jwtExceptionHandler(response, "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED.value()) 메서드를 사용하여 HTTP 상태 코드 401(UNAUTHORIZED)을 반환.
        if(token != null) {
            if(!jwtUtil.validateToken(token)){
                jwtExceptionHandler(response, "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED.value());
                return;
            }

            //토큰이 유효하면 jwtUtil.getUserInfoFromToken(token)을 사용하여 토큰에서 사용자 정보를 추출.
            Claims info = jwtUtil.getUserInfoFromToken(token);

            //Claims 객체에 저장된 사용자 정보는 "setAuthentication(info.getSubject())를 호출하여 사용자에 대한 인증을 설정하는 데 사용됩니다.
            setAuthentication(info.getSubject());
        }
        //doFilter(request, response)를 호출해서 체인의 다음 필터나 마지막 리소스를 호출.
        filterChain.doFilter(request,response);
    }





    /**
     * 인증 설정
     * <p>
     * 참고 문서 : <a href = "https://docs.spring.io/spring-security/site/docs/3.1.x/apidocs/org/springframework/security/core/context/SecurityContext.html">spring security 가이드 문서</a>
     *           <a href = "https://catsbi.oopy.io/f9b0d83c-4775-47da-9c81-2261851fe0d0">블로그 - 스프링 시큐리티 주요 아키텍처 이해</a>
     * </p>
     *
     * SecurityContext는 사용자의 역할, 권한 및 기타 세부 정보와 같은 사용자의 인증 및 권한 부여에 대한 정보를 보유.<br>
     * "SecurityContextHolder"는 현재 SecurityContext에 대한 액세스를 제공하는 클래스.<br>
     * <br>
     * 이 코드에서 "SecurityContext"의 인스턴스는 "SecurityContextHolder.createEmptyContext()"를 사용하여 생성되고<br>
     * 인증 토큰은 "context.setAuthentication(authentication)"을 사용하여 컨텍스트에 설정되며 채워진 보안 컨텍스트는 SecurityContextHolder.setContext(context)로 설정.<br>
     * 이걸 사용하는 이유는 이렇게 하면 나중에 어플리케이션에서 사용할 수 있도록 인증 토큰을 securityContext에 저장할 수 있기 때문.<br>

     * 정리하면, 사용자의 인증 정보를 저장한 Authentication 객체가 있음.<br>
     * 인증시에 id,password를 담고 인증 검증을 위해 사용됨.<br>
     * 인증이 끝나면 인증 결과물인 member 객체, 권한 정보를 securitycontext에 저장해서 전역적으로 사용이 가능해진다. */
    public void setAuthentication(String username) {
        //일단 SecurityContextHolder.createEmptyContext()를 통해 비어있는 securityContext를 만들어 줌.
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        //jwtUtil.createAuthentication(phoneNum)를 호출해서 가져온 번호를 가진 사용자에 대한 인증 토큰을 생성하고 context에 채워넣음.
        Authentication authentication = jwtUtil.createAuthentication(username);
        context.setAuthentication(authentication);

        //위에서 채워넣은 SecurityContext를 현재 SecurityContext로 사용함
        SecurityContextHolder.setContext(context);
    }

    /**
     * 토큰에 대한 오류 발생시 클라이언트에 에러 메시지를 커스터마이징 해서 보낸다
     * @param response 클라이언트에게 다시 보낼 응답 객체
     * @param msg 예외 사유를 나타내는 메시지
     * @param statusCode 오류를 나타내는 HTTP 상태 코드
     */

    public void jwtExceptionHandler(HttpServletResponse response, String msg, int statusCode) {
        //setStatus를 통해 response의 상태 코드 set
        response.setStatus(statusCode);
        //콘텐츠 유형을 json으로 바꿔줌
        response.setContentType("application/json");
        try {
            //ObjectMapper 클래스를 사용하여 클래스의 객체를 SecurityExceptionDtoJSON 문자열로 변환.
            //objectMapper는 자바 객체를 JSON으로 직렬화나 역직렬화 하는데 쓰이는 Jackson 라이브러리 클래스.
            //Jackson 라이브러리의 기본 클래스이며 Java 개체와 JSON 데이터 간의 변환 기능을 제공한다.
            String json = new ObjectMapper().writeValueAsString(new SecurityExceptionDto(statusCode, msg));
            //response의 body에 JSON 문자열이 작성됨
            response.getWriter().write(json);
        } catch (Exception e) {
            //예외를 처리하는 동안 발생하는 모든 예외(Exception)을 기록한다.
            log.error(e.getMessage());
        }
    }

}
