package com.tech.microservice.gateway.auth.controller;

import com.tech.microservice.gateway.auth.dto.LoginRequest;
import com.tech.microservice.gateway.auth.dto.UserRegistrationRequest;
import com.tech.microservice.gateway.auth.entity.User;
import com.tech.microservice.gateway.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final com.tech.microservice.gateway.auth.service.TokenBlacklistService tokenBlacklistService;
    private final com.tech.microservice.gateway.auth.config.JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService,
            com.tech.microservice.gateway.auth.service.TokenBlacklistService tokenBlacklistService,
            com.tech.microservice.gateway.auth.config.JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public User register(@RequestBody UserRegistrationRequest request) {
        return authService.register(
                request.username(),
                request.password(),
                request.role(),
                request.firstName(),
                request.lastName(),
                request.email());
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    @GetMapping("/profile")
    public String profile() {
        return "Profile info ....";
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            long ttl = jwtTokenProvider.getExpiration(jwt);
            if (ttl > 0) {
                tokenBlacklistService.blacklistToken(jwt, ttl);
            }
            return "Logged out successfully";
        }
        return "Invalid token";
    }

}
