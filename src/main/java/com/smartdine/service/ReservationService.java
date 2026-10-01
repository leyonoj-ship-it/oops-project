package com.smartdine.service;

import com.smartdine.dto.ReservationRequest;
import com.smartdine.model.Reservation;
import com.smartdine.model.ReservationStatus;

import java.util.List;

public interface ReservationService {
    Reservation createReservation(ReservationRequest request);
    Reservation getReservationById(Long reservationId);
    List<Reservation> getReservationsByCustomer(Long customerId);
    List<Reservation> getReservationsByEstablishment(Long establishmentId);
    Reservation updateReservationStatus(Long reservationId, ReservationStatus status);
}
