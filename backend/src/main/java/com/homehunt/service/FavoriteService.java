package com.homehunt.service;

import com.homehunt.dto.PropertyResponse;
import com.homehunt.entity.Favorite;
import com.homehunt.entity.Property;
import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.Role;
import com.homehunt.entity.User;
import com.homehunt.exception.ApiException;
import com.homehunt.repository.FavoriteRepository;
import com.homehunt.repository.PropertyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final MapperService mapperService;

    @Transactional
    public void add(Long propertyId) {
        User tenant = currentUserService.get();
        if (tenant.getRole() != Role.TENANT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only tenants can save favorites");
        }
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        if (property.getStatus() != PropertyStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Property is not available yet");
        }
        if (!favoriteRepository.existsByUserIdAndPropertyId(tenant.getId(), propertyId)) {
            favoriteRepository.save(Favorite.builder().user(tenant).property(property).build());
        }
    }

    @Transactional
    public void remove(Long propertyId) {
        User tenant = currentUserService.get();
        favoriteRepository.findByUserIdAndPropertyId(tenant.getId(), propertyId).ifPresent(favoriteRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> list() {
        User tenant = currentUserService.get();
        return favoriteRepository.findByUserId(tenant.getId()).stream()
                .map(Favorite::getProperty)
                .map(mapperService::toProperty)
                .toList();
    }
}
