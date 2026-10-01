package com.smartdine.service.impl;

import com.smartdine.dto.ReservationRequest;
import com.smartdine.exception.EstablishmentNotFoundException;
import com.smartdine.exception.ReservationException;
import com.smartdine.exception.UserNotFoundException;
import com.smartdine.model.*;
import com.smartdine.repository.CustomerRepository;
import com.smartdine.repository.DiningTableRepository;
import com.smartdine.repository.EstablishmentRepository;
import com.smartdine.repository.ReservationRepository;
import com.smartdine.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final EstablishmentRepository establishmentRepository;
    private final DiningTableRepository diningTableRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  CustomerRepository customerRepository,
                                  EstablishmentRepository establishmentRepository,
                                  DiningTableRepository diningTableRepository) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.establishmentRepository = establishmentRepository;
        this.diningTableRepository = diningTableRepository;
    }

    @Override
    public Reservation createReservation(ReservationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new UserNotFoundException(request.getCustomerId()));

        Establishment establishment = establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(request.getEstablishmentId()));

        DiningTable table = null;
        if (request.getTableNumber() != null) {
            table = diningTableRepository.findByEstablishment_EstablishmentIdAndTableNumber(
                    establishment.getEstablishmentId(), request.getTableNumber()).orElse(null);
        }

        Reservation reservation = new Reservation(
                customer,
                establishment,
                table,
                request.getReservationDate(),
                request.getReservationTime(),
                request.getNumberOfGuests(),
                request.getSelectedAmbiance()
        );

        reservation.setSpecialNotes(request.getSpecialNotes());

        if (request.getPreOrderedItems() != null && !request.getPreOrderedItems().isEmpty()) {
            reservation.setPreOrderedItems(new ArrayList<>(request.getPreOrderedItems()));
        }

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("Reservation not found with ID: " + reservationId));
    }

    @Override
    public List<Reservation> getReservationsByCustomer(Long customerId) {
        return reservationRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<Reservation> getReservationsByEstablishment(Long establishmentId) {
        return reservationRepository.findByEstablishment_EstablishmentIdOrderByReservationDateDesc(establishmentId);
    }

    @Override
    public Reservation updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }
}
