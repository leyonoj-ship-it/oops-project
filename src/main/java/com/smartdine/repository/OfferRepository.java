package com.smartdine.repository;

import com.smartdine.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByEstablishment_EstablishmentIdAndIsActiveTrue(Long establishmentId);

    List<Offer> findByIsActiveTrue();
}
