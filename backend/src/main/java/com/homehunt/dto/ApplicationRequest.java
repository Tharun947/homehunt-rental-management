package com.homehunt.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ApplicationRequest(@NotNull Long propertyId, @NotNull @DecimalMin("0.01") BigDecimal income, String notes) {
}
