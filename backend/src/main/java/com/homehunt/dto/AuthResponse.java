package com.homehunt.dto;

import com.homehunt.entity.Role;

public record AuthResponse(String token, Long id, String name, String email, Role role) {
}
