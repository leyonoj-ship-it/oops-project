package com.smartdine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * OOP COMPOSITION:
 * OrderItem represents a line item in an Order, encapsulating food details,
 * custom cooking preferences (e.g., "Less spicy, extra cheese"), unit price, and total.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "food_name", nullable = false, length = 150)
    private String foodName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(length = 255)
    private String customization; // e.g. "Less spicy", "Extra cheese", "No onion"

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    public OrderItem() {
    }

    public OrderItem(FoodItem foodItem, Integer quantity, String customization) {
        this.foodItem = foodItem;
        this.foodName = foodItem.getName();
        this.quantity = quantity;
        this.unitPrice = foodItem.getEffectivePrice();
        this.customization = customization;
        this.totalPrice = Math.round((this.unitPrice * quantity) * 100.0) / 100.0;
    }

    public OrderItem(FoodItem foodItem, Integer quantity) {
        this(foodItem, quantity, null);
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
        if (foodItem != null) {
            this.foodName = foodItem.getName();
        }
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalculateTotal();
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
        recalculateTotal();
    }

    public String getCustomization() {
        return customization;
    }

    public void setCustomization(String customization) {
        this.customization = customization;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    private void recalculateTotal() {
        if (this.unitPrice != null && this.quantity != null) {
            this.totalPrice = Math.round((this.unitPrice * this.quantity) * 100.0) / 100.0;
        }
    }

    @Override
    public String toString() {
        return foodName + " x" + quantity + " (₹" + totalPrice + ")" +
                (customization != null && !customization.isEmpty() ? " [" + customization + "]" : "");
    }
}
