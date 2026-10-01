package com.smartdine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OOP COMPOSITION:
 * Reservation links Customer, Establishment, DiningTable, Ambiance, and pre-ordered FoodItems.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonIgnoreProperties({"menu", "tables", "clearanceOffers", "hibernateLazyInitializer", "handler"})
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false, length = 10)
    private String reservationTime;

    @Column(name = "number_of_guests", nullable = false)
    private Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_ambiance", nullable = false, length = 50)
    private AmbianceType selectedAmbiance = AmbianceType.DEFAULT;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    // Optional pre-ordered items for the reservation
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reservation_food_items", joinColumns = @JoinColumn(name = "reservation_id"))
    @Column(name = "food_item_detail")
    private List<String> preOrderedItems = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Reservation() {
    }

    public Reservation(Customer customer, Establishment establishment, DiningTable table,
                       LocalDate reservationDate, String reservationTime, Integer numberOfGuests,
                       AmbianceType selectedAmbiance) {
        this.customer = customer;
        this.establishment = establishment;
        this.table = table;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.numberOfGuests = numberOfGuests;
        this.selectedAmbiance = selectedAmbiance != null ? selectedAmbiance : AmbianceType.DEFAULT;
        this.status = ReservationStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public DiningTable getTable() {
        return table;
    }

    public void setTable(DiningTable table) {
        this.table = table;
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

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public List<String> getPreOrderedItems() {
        return preOrderedItems;
    }

    public void setPreOrderedItems(List<String> preOrderedItems) {
        this.preOrderedItems = preOrderedItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + " for " + numberOfGuests + " guests at " +
                (establishment != null ? establishment.getName() : "Establishment") + " (" + selectedAmbiance + ")";
    }
}
