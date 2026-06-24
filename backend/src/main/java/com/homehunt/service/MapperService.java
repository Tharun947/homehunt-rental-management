package com.homehunt.service;

import com.homehunt.dto.ApplicationResponse;
import com.homehunt.dto.PropertyResponse;
import com.homehunt.dto.UserResponse;
import com.homehunt.entity.Property;
import com.homehunt.entity.RentalApplication;
import com.homehunt.entity.User;
import org.springframework.stereotype.Service;

@Service
public class MapperService {
    public UserResponse toUser(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public PropertyResponse toProperty(Property property) {
        return toProperty(property, true);
    }

    public PropertyResponse toProperty(Property property, boolean includeLandlordContact) {
        User owner = property.getOwner();
        return new PropertyResponse(
                property.getId(),
                property.getTitle(),
                property.getDescription(),
                property.getLocation(),
                property.getPrice(),
                property.getType(),
                property.getImageUrl(),
                property.getStatus(),
                owner.getId(),
                includeLandlordContact ? owner.getName() : "Login required",
                includeLandlordContact ? owner.getEmail() : null,
                property.getCreatedAt());
    }

    public ApplicationResponse toApplication(RentalApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getUser().getId(),
                application.getUser().getName(),
                application.getProperty().getId(),
                application.getProperty().getTitle(),
                application.getStatus(),
                application.getIncome(),
                application.getNotes(),
                application.getCreatedAt());
    }
}
