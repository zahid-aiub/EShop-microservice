package com.tech.microservice.gateway.auth.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private SecretKey JWT_SECRET;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void init() {
        // decode base64 secret and create strong key
        JWT_SECRET = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(String username, String role) {
        long JWT_EXPIRATION = 1000 * 60 * 60;
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(JWT_SECRET)
                .compact();
    }

}