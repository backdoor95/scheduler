package com.fastcampus.minischeduler.security;


import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        String prefixJwt = request.getHeader(JwtTokenProvider.HEADER); // 헤더에서 토큰 받아오기

        if (prefixJwt == null) { // 토큰이 없는 경우
            chain.doFilter(request, response);
            return;
        }

        String jwt = prefixJwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");

        try {
            DecodedJWT decodedJWT = JwtTokenProvider.verify(jwt);
            Long id = decodedJWT.getClaim("id").asLong();
            String roles = decodedJWT.getClaim("role").asString();

            User user = User.builder()
                            .id(id)
                            .roles(roles)
                            .build();

            MyUserDetails myUserDetails = new MyUserDetails(user);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            myUserDetails,
                            myUserDetails.getPassword(),
                            myUserDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("DEBUG -- 인증 객체 생성");
        } catch (SignatureVerificationException sve) {
            log.error("토큰 검증 실패");
        } catch (TokenExpiredException tee) {
            log.error("토큰 만료");
        } finally {
            chain.doFilter(request, response);
        }
    }
}
