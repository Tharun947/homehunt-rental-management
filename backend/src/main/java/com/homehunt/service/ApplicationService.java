package com.homehunt.service;

import com.homehunt.dto.ApplicationRequest;
import com.homehunt.dto.ApplicationResponse;
import com.homehunt.entity.ApplicationStatus;
import com.homehunt.entity.Property;
import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.RentalApplication;
import com.homehunt.entity.Role;
import com.homehunt.entity.User;
import com.homehunt.exception.ApiException;
import com.homehunt.repository.ApplicationRepository;
import com.homehunt.repository.PropertyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final MapperService mapperService;

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request) {
        User tenant = currentUserService.get();
        if (tenant.getRole() != Role.TENANT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only tenants can apply");
        }
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        if (property.getStatus() != PropertyStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Property is not available for applications yet");
        }
        if (applicationRepository.existsByUserIdAndPropertyId(tenant.getId(), property.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already applied for this property");
        }
        RentalApplication application = RentalApplication.builder()
                .user(tenant)
                .property(property)
                .status(ApplicationStatus.PENDING)
                .income(request.income())
                .notes(request.notes())
                .build();
        return mapperService.toApplication(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> mine() {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(currentUserService.get().getId()).stream()
                .map(mapperService::toApplication)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> forProperty(Long propertyId) {
        User landlord = currentUserService.get();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        if (landlord.getRole() != Role.ADMIN && !property.getOwner().getId().equals(landlord.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only view applications for your properties");
        }
        return applicationRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId).stream()
                .map(mapperService::toApplication)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> forMyProperties() {
        User landlord = currentUserService.get();
        if (landlord.getRole() == Role.ADMIN) {
            return applicationRepository.findAll().stream()
                    .map(mapperService::toApplication)
                    .toList();
        }
        if (landlord.getRole() != Role.LANDLORD) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only landlords can view property applications");
        }
        return applicationRepository.findByPropertyOwnerIdOrderByCreatedAtDesc(landlord.getId()).stream()
                .map(mapperService::toApplication)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatus status) {
        User landlord = currentUserService.get();
        RentalApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found"));
        if (landlord.getRole() != Role.ADMIN && !application.getProperty().getOwner().getId().equals(landlord.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage applications for your properties");
        }
        application.setStatus(status);
        if (status == ApplicationStatus.ACCEPTED) {
            applicationRepository.findByPropertyId(application.getProperty().getId()).stream()
                    .filter(other -> !other.getId().equals(application.getId()))
                    .forEach(other -> other.setStatus(ApplicationStatus.REJECTED));
        }
        return mapperService.toApplication(application);
    }

    @Transactional
    public void withdraw(Long id) {
        User tenant = currentUserService.get();
        RentalApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found"));
        if (tenant.getRole() != Role.ADMIN && !application.getUser().getId().equals(tenant.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only remove your own applications");
        }
        applicationRepository.delete(application);
    }
}
