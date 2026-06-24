package com.homehunt.dto;

import com.homehunt.entity.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PropertyRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String location,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull PropertyType type,
        String imageUrl) {
}
