package com.homehunt.dto;

import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.PropertyType;
import java.math.BigDecimal;
import java.time.Instant;

public record PropertyResponse(
        Long id,
        String title,
        String description,
        String location,
        BigDecimal price,
        PropertyType type,
        String imageUrl,
        PropertyStatus status,
        Long ownerId,
        String landlordName,
        String landlordEmail,
        Instant createdAt) {
}
