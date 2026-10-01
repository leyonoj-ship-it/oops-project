package com.smartdine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClearanceOfferRequest {

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    @NotNull(message = "Food item ID is required")
    private Long foodId;

    @NotNull(message = "Discount percentage is required")
    @Min(value = 10, message = "Minimum clearance discount is 10%")
    @Max(value = 90, message = "Maximum clearance discount is 90%")
    private Integer discountPercentage = 50;

    @NotNull(message = "Available clearance quantity is required")
    @Min(value = 1, message = "Clearance quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Start time is required (e.g. 21:00)")
    private String startTime;

    @NotBlank(message = "End time is required (e.g. 22:30)")
    private String endTime;

    public ClearanceOfferRequest() {
    }

    public Long getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
}
