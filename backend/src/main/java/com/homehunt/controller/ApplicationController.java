package com.homehunt.controller;

import com.homehunt.dto.ApplicationRequest;
import com.homehunt.dto.ApplicationResponse;
import com.homehunt.dto.ApplicationStatusRequest;
import com.homehunt.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    ApplicationResponse apply(@Valid @RequestBody ApplicationRequest request) {
        return applicationService.apply(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TENANT')")
    List<ApplicationResponse> mine() {
        return applicationService.mine();
    }

    @GetMapping("/property/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD','ADMIN')")
    List<ApplicationResponse> forProperty(@PathVariable Long id) {
        return applicationService.forProperty(id);
    }

    @GetMapping("/landlord")
    @PreAuthorize("hasAnyRole('LANDLORD','ADMIN')")
    List<ApplicationResponse> forMyProperties() {
        return applicationService.forMyProperties();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LANDLORD','ADMIN')")
    ApplicationResponse updateStatus(@PathVariable Long id, @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT','ADMIN')")
    void withdraw(@PathVariable Long id) {
        applicationService.withdraw(id);
    }
}
