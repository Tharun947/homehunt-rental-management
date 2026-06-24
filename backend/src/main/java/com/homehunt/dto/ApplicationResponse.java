package com.homehunt.dto;

import com.homehunt.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationResponse(
        Long id,
        Long userId,
        String tenantName,
        Long propertyId,
        String propertyTitle,
        ApplicationStatus status,
        BigDecimal income,
        String notes,
        Instant createdAt) {
}
