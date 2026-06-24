package com.homehunt.controller;

import com.homehunt.dto.DashboardStatsResponse;
import com.homehunt.dto.PropertyResponse;
import com.homehunt.dto.RoleUpdateRequest;
import com.homehunt.dto.UserResponse;
import com.homehunt.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/stats")
    DashboardStatsResponse stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    List<UserResponse> users() {
        return adminService.users();
    }

    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

    @PutMapping("/users/{id}/role")
    UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return adminService.updateRole(id, request.role());
    }

    @GetMapping("/properties")
    Page<PropertyResponse> properties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return adminService.properties(PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @DeleteMapping("/properties/{id}")
    void deleteProperty(@PathVariable Long id) {
        adminService.deleteProperty(id);
    }
}
