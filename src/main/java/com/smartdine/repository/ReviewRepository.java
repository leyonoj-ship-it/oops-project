package com.smartdine.repository;

import com.smartdine.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEstablishment_EstablishmentIdOrderByCreatedAtDesc(Long establishmentId);

    List<Review> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    List<Review> findByIsFlaggedTrueOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(AVG(r.rating), 4.0) FROM Review r WHERE r.establishment.establishmentId = :establishmentId")
    Double calculateAverageRating(@Param("establishmentId") Long establishmentId);

    long countByEstablishment_EstablishmentId(Long establishmentId);
}
