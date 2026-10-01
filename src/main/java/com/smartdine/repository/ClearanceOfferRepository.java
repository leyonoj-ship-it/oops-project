package com.smartdine.repository;

import com.smartdine.model.ClearanceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClearanceOfferRepository extends JpaRepository<ClearanceOffer, Long> {

    List<ClearanceOffer> findByEstablishment_EstablishmentIdAndIsActiveTrue(Long establishmentId);

    List<ClearanceOffer> findByIsActiveTrueAndRemainingQuantityGreaterThan(Integer minQuantity);

    @Query("SELECT c FROM ClearanceOffer c WHERE c.isActive = true AND c.remainingQuantity > 0")
    List<ClearanceOffer> findAllActiveClearances();
}
