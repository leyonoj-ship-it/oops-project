package com.smartdine.dto;

import com.smartdine.model.AmbianceType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class ReservationRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    private Integer tableNumber;

    @NotNull(message = "Reservation date is required")
    @FutureOrPresent(message = "Reservation date cannot be in the past")
    private LocalDate reservationDate;

    @NotBlank(message = "Reservation time is required (e.g. 19:30)")
    private String reservationTime;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest is required")
    private Integer numberOfGuests;

    @NotNull(message = "Selected ambiance is required")
    private AmbianceType selectedAmbiance = AmbianceType.DEFAULT;

    private String specialNotes;

    private List<String> preOrderedItems;

    public ReservationRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public AmbianceType getSelectedAmbiance() {
        return selectedAmbiance;
    }

    public void setSelectedAmbiance(AmbianceType selectedAmbiance) {
        this.selectedAmbiance = selectedAmbiance;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public List<String> getPreOrderedItems() {
        return preOrderedItems;
    }

    public void setPreOrderedItems(List<String> preOrderedItems) {
        this.preOrderedItems = preOrderedItems;
    }
}
