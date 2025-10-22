package com.tech.microservice.gateway.auth.controller;


import com.tech.microservice.gateway.auth.dto.LoginRequest;
import com.tech.microservice.gateway.auth.dto.UserRegistrationRequest;
import com.tech.microservice.gateway.auth.entity.User;
import com.tech.microservice.gateway.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody UserRegistrationRequest request) {
        return authService.register(
                request.username(),
                request.password(),
                request.role(),
                request.firstName(),
                request.lastName(),
                request.email()
        );
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

}
