package com.smartdine.repository;

import com.smartdine.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    List<DiningTable> findByEstablishment_EstablishmentId(Long establishmentId);

    Optional<DiningTable> findByEstablishment_EstablishmentIdAndTableNumber(Long establishmentId, Integer tableNumber);

    List<DiningTable> findByEstablishment_EstablishmentIdAndIsAvailableTrue(Long establishmentId);
}
