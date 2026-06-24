package com.homehunt.repository;

import com.homehunt.entity.RentalApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<RentalApplication, Long> {
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);

    List<RentalApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<RentalApplication> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    List<RentalApplication> findByPropertyId(Long propertyId);

    List<RentalApplication> findByPropertyOwnerIdOrderByCreatedAtDesc(Long ownerId);

    void deleteByUserId(Long userId);

    void deleteByPropertyId(Long propertyId);
}
