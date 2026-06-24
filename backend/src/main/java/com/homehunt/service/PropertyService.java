package com.homehunt.service;

import com.homehunt.dto.PropertyRequest;
import com.homehunt.dto.PropertyResponse;
import com.homehunt.entity.Property;
import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.PropertyType;
import com.homehunt.entity.Role;
import com.homehunt.entity.User;
import com.homehunt.exception.ApiException;
import com.homehunt.repository.PropertyRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final MapperService mapperService;

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        User owner = currentUserService.get();
        if (owner.getRole() != Role.LANDLORD && owner.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only landlords or admins can create properties");
        }
        Property property = Property.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .price(request.price())
                .type(request.type())
                .imageUrl(request.imageUrl())
                .status(owner.getRole() == Role.ADMIN ? PropertyStatus.APPROVED : PropertyStatus.PENDING)
                .owner(owner)
                .build();
        return mapperService.toProperty(propertyRepository.save(property));
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> publicSearch(String query, String location, BigDecimal minPrice, BigDecimal maxPrice,
            PropertyType type, Pageable pageable) {
        return propertyRepository.findAll(spec(query, location, minPrice, maxPrice, type, PropertyStatus.APPROVED), pageable)
                .map(property -> mapperService.toProperty(property, isAuthenticated()));
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPublic(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        if (property.getStatus() != PropertyStatus.APPROVED && !canViewPrivate(property)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Property not found");
        }
        return mapperService.toProperty(property, isAuthenticated());
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> mine(Pageable pageable) {
        User owner = currentUserService.get();
        return propertyRepository.findAll((root, query, cb) -> cb.equal(root.get("owner").get("id"), owner.getId()), pageable)
                .map(mapperService::toProperty);
    }

    @Transactional
    public PropertyResponse update(Long id, PropertyRequest request) {
        Property property = ownedProperty(id);
        property.setTitle(request.title());
        property.setDescription(request.description());
        property.setLocation(request.location());
        property.setPrice(request.price());
        property.setType(request.type());
        property.setImageUrl(request.imageUrl());
        property.setStatus(currentUserService.get().getRole() == Role.ADMIN ? PropertyStatus.APPROVED : PropertyStatus.PENDING);
        return mapperService.toProperty(property);
    }

    @Transactional
    public void delete(Long id) {
        propertyRepository.delete(ownedProperty(id));
    }

    @Transactional
    public PropertyResponse approve(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        property.setStatus(PropertyStatus.APPROVED);
        return mapperService.toProperty(property);
    }

    @Transactional
    public PropertyResponse reject(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        property.setStatus(PropertyStatus.REJECTED);
        return mapperService.toProperty(property);
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> pending(Pageable pageable) {
        return propertyRepository.findAll((root, query, cb) -> cb.equal(root.get("status"), PropertyStatus.PENDING), pageable)
                .map(mapperService::toProperty);
    }

    private Property ownedProperty(Long id) {
        User owner = currentUserService.get();
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        if (!property.getOwner().getId().equals(owner.getId()) && owner.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own properties");
        }
        return property;
    }

    private boolean canViewPrivate(Property property) {
        try {
            User user = currentUserService.get();
            return user.getRole() == Role.ADMIN || property.getOwner().getId().equals(user.getId());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean isAuthenticated() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Specification<Property> spec(String query, String location, BigDecimal minPrice, BigDecimal maxPrice,
            PropertyType type, PropertyStatus status) {
        return (root, criteriaQuery, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (query != null && !query.isBlank()) {
                String keyword = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("location")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword),
                        cb.like(cb.lower(root.get("owner").get("name")), keyword)));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
