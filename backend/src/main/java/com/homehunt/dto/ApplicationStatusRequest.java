package com.homehunt.dto;

import com.homehunt.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull ApplicationStatus status) {
}
