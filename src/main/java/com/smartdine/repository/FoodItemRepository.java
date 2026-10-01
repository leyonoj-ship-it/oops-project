package com.smartdine.repository;

import com.smartdine.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByEstablishment_EstablishmentId(Long establishmentId);

    List<FoodItem> findByEstablishment_EstablishmentIdAndIsAvailableTrue(Long establishmentId);

    @Query("SELECT f FROM FoodItem f WHERE :tag MEMBER OF f.tags AND f.isAvailable = true")
    List<FoodItem> findByAmbianceTag(@Param("tag") String tag);

    @Query("SELECT f FROM FoodItem f WHERE f.establishment.establishmentId = :establishmentId AND :tag MEMBER OF f.tags AND f.isAvailable = true")
    List<FoodItem> findByEstablishmentAndAmbianceTag(@Param("establishmentId") Long establishmentId, @Param("tag") String tag);

    List<FoodItem> findByClearancePriceIsNotNullAndIsAvailableTrue();

    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) AND f.isAvailable = true")
    List<FoodItem> searchFoodItems(@Param("query") String query);
}
