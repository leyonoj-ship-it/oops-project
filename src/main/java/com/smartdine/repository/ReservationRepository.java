package com.smartdine.repository;

import com.smartdine.model.Reservation;
import com.smartdine.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    List<Reservation> findByEstablishment_EstablishmentIdOrderByReservationDateDesc(Long establishmentId);

    List<Reservation> findByEstablishment_EstablishmentIdAndStatus(Long establishmentId, ReservationStatus status);

    List<Reservation> findByEstablishment_EstablishmentIdAndReservationDate(Long establishmentId, LocalDate date);

    long countByEstablishment_EstablishmentIdAndStatus(Long establishmentId, ReservationStatus status);
}
