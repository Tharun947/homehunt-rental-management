package com.homehunt.controller;

import com.homehunt.dto.PropertyResponse;
import com.homehunt.service.FavoriteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{propertyId}")
    @PreAuthorize("hasRole('TENANT')")
    void add(@PathVariable Long propertyId) {
        favoriteService.add(propertyId);
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('TENANT')")
    void remove(@PathVariable Long propertyId) {
        favoriteService.remove(propertyId);
    }

    @GetMapping
    @PreAuthorize("hasRole('TENANT')")
    List<PropertyResponse> list() {
        return favoriteService.list();
    }
}
