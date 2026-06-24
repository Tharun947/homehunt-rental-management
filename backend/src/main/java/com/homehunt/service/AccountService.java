package com.homehunt.service;

import com.homehunt.dto.AuthResponse;
import com.homehunt.dto.PasswordUpdateRequest;
import com.homehunt.dto.ProfileUpdateRequest;
import com.homehunt.dto.UserResponse;
import com.homehunt.entity.User;
import com.homehunt.exception.ApiException;
import com.homehunt.repository.UserRepository;
import com.homehunt.security.JwtService;
import com.homehunt.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public UserResponse me() {
        return mapperService.toUser(currentUserService.get());
    }

    @Transactional
    public AuthResponse updateProfile(ProfileUpdateRequest request) {
        User user = currentUserService.get();
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
                });
        user.setName(request.name().trim());
        user.setEmail(email);
        return authResponse(user);
    }

    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        User user = currentUserService.get();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    private AuthResponse authResponse(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
