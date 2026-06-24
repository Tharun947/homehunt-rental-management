package com.homehunt.controller;

import com.homehunt.dto.PropertyRequest;
import com.homehunt.dto.PropertyResponse;
import com.homehunt.entity.PropertyType;
import com.homehunt.service.PropertyService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD')")
    PropertyResponse create(@Valid @RequestBody PropertyRequest request) {
        return propertyService.create(request);
    }

    @GetMapping
    Page<PropertyResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return propertyService.publicSearch(q, location, minPrice, maxPrice, type, pageable(page, size, sort));
    }

    @GetMapping("/{id}")
    PropertyResponse get(@PathVariable Long id) {
        return propertyService.getPublic(id);
    }

    @GetMapping({"/my", "/mine"})
    @PreAuthorize("hasRole('LANDLORD')")
    Page<PropertyResponse> mine(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return propertyService.mine(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD','ADMIN')")
    PropertyResponse update(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        return propertyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD','ADMIN')")
    void delete(@PathVariable Long id) {
        propertyService.delete(id);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    PropertyResponse approve(@PathVariable Long id) {
        return propertyService.approve(id);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    PropertyResponse reject(@PathVariable Long id) {
        return propertyService.reject(id);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    Page<PropertyResponse> pending(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return propertyService.pending(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    private Pageable pageable(int page, int size, String sort) {
        Sort selected = switch (sort) {
            case "price" -> Sort.by(Sort.Direction.ASC, "price");
            case "-price" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        return PageRequest.of(page, Math.min(size, 50), selected);
    }
}
