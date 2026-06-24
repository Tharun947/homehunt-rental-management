package com.homehunt.controller;

import com.homehunt.dto.AuthResponse;
import com.homehunt.dto.PasswordUpdateRequest;
import com.homehunt.dto.ProfileUpdateRequest;
import com.homehunt.dto.UserResponse;
import com.homehunt.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/me")
    UserResponse me() {
        return accountService.me();
    }

    @PutMapping("/profile")
    AuthResponse updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return accountService.updateProfile(request);
    }

    @PutMapping("/password")
    void updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        accountService.updatePassword(request);
    }
}
