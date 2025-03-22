package com.kyn.spring_backend.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret:ZnJlZWJvYXJkc2VjcmV0a2V5ZnJlZWJvYXJkc2VjcmV0a2V5ZnJlZWJvYXJkc2VjcmV0a2V5}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private long expiration;

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public long getExpiration() {
        return expiration;
    }
}