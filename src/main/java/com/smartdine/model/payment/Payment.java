package com.smartdine.model.payment;

import com.smartdine.model.PaymentMethod;

/**
 * OOP INTERFACE & POLYMORPHISM CONTRACT:
 * Contract for all payment types in SmartDine (Cash and UPI).
 * Polymorphic dispatch enables seamless processing regardless of underlying payment channel.
 */
public interface Payment {

    /**
     * Executes the payment workflow polymorphically.
     * @param amount the billing amount in INR
     * @param referenceDetails VPA/UPI ID for UPI, or Cashier/Table note for Cash
     * @return PaymentResult encapsulation
     */
    PaymentResult processPayment(double amount, String referenceDetails);

    PaymentMethod getPaymentMethod();

    default boolean validateAmount(double amount) {
        return amount > 0.0;
    }
}
