package com.smartdine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * UNIQUE SMARTDINE FEATURE:
 * Smart Food Clearance Alert / Closing Time Food Offer.
 * Dynamically discounts remaining portions near closing time to eliminate food waste.
 * Enforces quantity bounds and auto-expiration.
 */
@Entity
@Table(name = "clearance_offers")
public class ClearanceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonIgnoreProperties({"menu", "tables", "clearanceOffers", "hibernateLazyInitializer", "handler"})
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "discount_percentage", nullable = false)
    private Integer discountPercentage = 50; // e.g. 50%

    @Column(name = "original_price", nullable = false)
    private Double originalPrice;

    @Column(name = "clearance_price", nullable = false)
    private Double clearancePrice;

    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime; // e.g. "21:00"

    @Column(name = "end_time", nullable = false, length = 10)
    private String endTime; // e.g. "22:30"

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ClearanceOffer() {
    }

    public ClearanceOffer(Establishment establishment, FoodItem foodItem, Integer discountPercentage,
                          Integer quantity, String startTime, String endTime) {
        this.establishment = establishment;
        this.foodItem = foodItem;
        this.discountPercentage = discountPercentage;
        this.originalPrice = foodItem.getPrice();
        this.clearancePrice = Math.round((originalPrice * (1.0 - (discountPercentage / 100.0))) * 100.0) / 100.0;
        this.initialQuantity = quantity;
        this.remainingQuantity = quantity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Business Logic: Check if currently valid based on time and remaining quantity
    public boolean isCurrentlyValid() {
        if (!isActive || remainingQuantity <= 0) {
            return false;
        }
        try {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(this.startTime);
            LocalTime end = LocalTime.parse(this.endTime);
            if (end.isBefore(start)) {
                return now.isAfter(start) || now.isBefore(end);
            }
            return now.isAfter(start) && now.isBefore(end);
        } catch (Exception e) {
            return true;
        }
    }

    // Decrement remaining portions when bought
    public boolean deductClearancePortion(int count) {
        if (remainingQuantity >= count) {
            remainingQuantity -= count;
            if (remainingQuantity == 0) {
                this.isActive = false;
                if (foodItem != null) {
                    foodItem.setClearancePrice(null);
                }
            }
            return true;
        }
        return false;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getClearancePrice() {
        return clearancePrice;
    }

    public void setClearancePrice(Double clearancePrice) {
        this.clearancePrice = clearancePrice;
    }

    public Integer getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(Integer initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
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
        if (!(o instanceof ClearanceOffer that)) return false;
        return Objects.equals(offerId, that.offerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offerId);
    }
}
