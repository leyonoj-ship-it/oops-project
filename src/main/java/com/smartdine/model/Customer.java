package com.smartdine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * OOP INHERITANCE:
 * Customer extends Person, inheriting identity, email, phone, and role management,
 * while encapsulating customer-specific properties like loyalty points and preferred ambiance.
 */
@Entity
@Table(name = "customers")
public class Customer extends Person {

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "loyalty_points")
    private int loyaltyPoints = 0;

    @Column(name = "preferred_ambiance", length = 50)
    private String preferredAmbiance = "DEFAULT";

    public Customer() {
        super();
        setRole(UserRole.CUSTOMER);
    }

    // Overloaded Constructor
    public Customer(String name, String email, String phone) {
        super(name, email, phone, UserRole.CUSTOMER);
        this.loyaltyPoints = 0;
        this.preferredAmbiance = "DEFAULT";
    }

    // Overloaded Constructor with address
    public Customer(String name, String email, String phone, String deliveryAddress) {
        super(name, email, phone, UserRole.CUSTOMER);
        this.deliveryAddress = deliveryAddress;
        this.loyaltyPoints = 0;
    }

    // METHOD OVERRIDING (Polymorphic method implementations)
    @Override
    public String getRoleDescription() {
        return "Customer: Can discover establishments, reserve tables, order food, and use Fast Serve.";
    }

    @Override
    public boolean canPerformAdminActions() {
        return false;
    }

    @Override
    public boolean canManageEstablishment() {
        return false;
    }

    // Customer-specific business methods
    public void addLoyaltyPoints(int points) {
        if (points > 0) {
            this.loyaltyPoints += points;
        }
    }

    public boolean redeemLoyaltyPoints(int points) {
        if (points > 0 && this.loyaltyPoints >= points) {
            this.loyaltyPoints -= points;
            return true;
        }
        return false;
    }

    // Encapsulation: Getters and Setters
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getPreferredAmbiance() {
        return preferredAmbiance;
    }

    public void setPreferredAmbiance(String preferredAmbiance) {
        this.preferredAmbiance = preferredAmbiance;
    }
}
