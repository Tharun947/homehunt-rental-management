package com.homehunt.repository;

import com.homehunt.entity.Property;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {
    long countByStatus(com.homehunt.entity.PropertyStatus status);

    List<Property> findByOwnerId(Long ownerId);
}
