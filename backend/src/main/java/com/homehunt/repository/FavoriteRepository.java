package com.homehunt.repository;

import com.homehunt.entity.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);

    Optional<Favorite> findByUserIdAndPropertyId(Long userId, Long propertyId);

    List<Favorite> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByPropertyId(Long propertyId);
}
