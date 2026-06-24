package com.homehunt.dto;

import com.homehunt.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) {
}
