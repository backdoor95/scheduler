package com.fastcampus.minischeduler.core.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.user.Role;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final Long EXP = 1000L * 60 * 60 * 2; // 2시간
    public static final String TOKEN_PREFIX = "Bearer "; // 스페이스 필요함
    public static final String HEADER = "Authorization";
    public static String SECRET;

    @Value("${my-env.jwt.secret}")
    public void setSECRET(String secret) {
        SECRET = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String create(User user) {

        String jwt = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXP))
                .withClaim("id", user.getId())
                .withClaim("role", user.getRole().name())
                .withClaim("email", user.getEmail())
                .withClaim("fullName", user.getFullName())
                .withClaim("profileImage", user.getProfileImage())
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwt;
    }

    public DecodedJWT verify(String jwt) throws SignatureVerificationException, TokenExpiredException {

        return  JWT.require(Algorithm.HMAC512(SECRET))
                .build()
                .verify(jwt);
    }

    public Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET))
                .build()
                .verify(token.replace(TOKEN_PREFIX, ""));
        return decodedJWT.getClaim("id").asLong();
    }

    public UserResponse.UserDto getUserInfo(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET))
                .build()
                .verify(token.replace(TOKEN_PREFIX, ""));

        return UserResponse.UserDto.builder()
                .id(decodedJWT.getClaim("id").asLong())
                .email(decodedJWT.getClaim("email").asString())
                .fullName(decodedJWT.getClaim("fullName").asString())
                .role(Role.valueOf(decodedJWT.getClaim("role").asString()))
                .profileImage(decodedJWT.getClaim("profileImage").asString())
                .build();
    }
}