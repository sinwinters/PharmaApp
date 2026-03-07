package com.pharma.application.dto;

public record UserDto(Long id, String username, String email, String roleName, Boolean enabled) {}
