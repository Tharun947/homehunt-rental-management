package com.homehunt.service;

import com.homehunt.dto.DashboardStatsResponse;
import com.homehunt.dto.PropertyResponse;
import com.homehunt.dto.UserResponse;
import com.homehunt.entity.Property;
import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.Role;
import com.homehunt.entity.User;
import com.homehunt.exception.ApiException;
import com.homehunt.repository.ApplicationRepository;
import com.homehunt.repository.FavoriteRepository;
import com.homehunt.repository.PropertyRepository;
import com.homehunt.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ApplicationRepository applicationRepository;
    private final FavoriteRepository favoriteRepository;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse stats() {
        return new DashboardStatsResponse(
                userRepository.count(),
                propertyRepository.count(),
                propertyRepository.countByStatus(PropertyStatus.PENDING));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return userRepository.findAll().stream().map(mapperService::toUser).toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        for (Property property : propertyRepository.findByOwnerId(user.getId())) {
            applicationRepository.deleteByPropertyId(property.getId());
            favoriteRepository.deleteByPropertyId(property.getId());
            propertyRepository.delete(property);
        }

        applicationRepository.deleteByUserId(user.getId());
        favoriteRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setRole(role);
        return mapperService.toUser(user);
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> properties(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(mapperService::toProperty);
    }

    @Transactional
    public void deleteProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        applicationRepository.deleteByPropertyId(property.getId());
        favoriteRepository.deleteByPropertyId(property.getId());
        propertyRepository.delete(property);
    }
}
