package com.sample.operator.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String accessible = "127.0.0.1"; // 우선 특정 IP 만으로 접근 가능하게 거르는 기능



    // 스프링 시큐리티에서 사용한 패스워드 인코더
    // 계정의 비번을 인코딩할때 디폴트로 채용할 빈 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 로그아웃 설정
        http.logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .addLogoutHandler((req, res, auth) -> { // 로그아웃 핸들러 추가 세션 무효화
                    HttpSession session = req.getSession();
                    session.invalidate();
                })
                .logoutSuccessHandler((req, res, auth) -> { // 로그아웃 성공 핸들러 추가 리디렉션
                    res.sendRedirect("/");
                })
                .invalidateHttpSession(true) // 세션 초기화
        );

        // 인가없는 페이지 접근 시 에러 페이지 경로 설정
        http.exceptionHandling(securityExceptionHandlingConfigurer -> securityExceptionHandlingConfigurer
                .authenticationEntryPoint((req, res, auth) -> res.sendRedirect("denied"))
                .accessDeniedPage("/denied"));

        // 인증 / 인가 설정 , 세션 설정
        // 설정 순서 중요
        // 먼저 선언된 설정일수록 우선순위 높음
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/sign_up", "/sign_in", "/denied", "/assets/**").permitAll() // 무조건 허용
                        .requestMatchers("/adm/**").hasAuthority("ADMIN")
                        .requestMatchers("/ssl/**", "/pgp/**").hasAuthority("OPERATION")
                        .requestMatchers("/member/**").hasAuthority("MEMBER")
                        .requestMatchers("/**").access(isAccessibleIp()) // 그 외 모든 접근은 허용 IP만
                        .anyRequest().authenticated() // 그 외 모든 접근은 인가 필요

                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(httpSecurityFormLoginConfigurer -> {
                    httpSecurityFormLoginConfigurer.loginPage("/sign_in")
                            .loginProcessingUrl("/login").usernameParameter("accountName").passwordParameter("password")
                            .failureHandler((req, res, ex) -> res.sendRedirect("/sign_in?alertMsg=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8)))
                            .successForwardUrl("/").permitAll();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false) //true 면 새 로그인 차단
                        .expiredUrl("sign_in"));

        return http.build();
    }

    // IP 사용 인가 설정
    private AuthorizationManager<RequestAuthorizationContext> isAccessibleIp()
    {
      return (auth, context) -> {
          HttpServletRequest req = context.getRequest();
          
          String ip = getClientIp(req);
          if( ip == null || ip.isEmpty() ){
              // 접속 불가한 IP
              return new AuthorizationDecision(false);
          }


          IpAddressMatcher matcher = new IpAddressMatcher(ip);
          
          if(!matcher.matches(req)){
              return new AuthorizationDecision(false);
          }
          
          // 여기까지 오면 접근 가능 IP
          return new AuthorizationDecision(true);
      };
    }
    
    // 프록시 이용할 경우를 고려하여 IP 추출
    private String getClientIp(HttpServletRequest request) {
        String[] ipInfoHeaders = new String[]{
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getHeader("Proxy-Client-IP"),
                request.getHeader("WL-Proxy-Client-IP"),
                request.getHeader("HTTP_CLIENT_IP"),
                request.getHeader("HTTP_X_FORWARDED_FOR"),
                request.getRemoteAddr(),
        };
        
        return Arrays.stream(ipInfoHeaders)
                .filter(ip -> ip != null && (ip.startsWith(accessible)) )
                .findFirst().orElse(null);// 조건에 맞는 ip 있으면 ip 리턴, 없으면 null 리턴
    }
}
