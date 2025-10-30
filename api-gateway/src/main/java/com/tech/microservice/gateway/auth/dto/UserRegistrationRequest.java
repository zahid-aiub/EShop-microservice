package com.tech.microservice.gateway.auth.dto;


public record UserRegistrationRequest(
        String firstName,
        String lastName,
        String email,
        String username,
        String password,
        String role
) {}
