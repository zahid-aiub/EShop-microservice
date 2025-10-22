package com.tech.microservice.auth.controller;


import com.tech.microservice.auth.dto.LoginRequest;
import com.tech.microservice.auth.dto.UserRegistrationRequest;
import com.tech.microservice.auth.entity.User;
import com.tech.microservice.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

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
