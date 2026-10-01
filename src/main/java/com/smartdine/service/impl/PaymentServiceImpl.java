package com.smartdine.service.impl;

import com.smartdine.exception.InvalidPaymentException;
import com.smartdine.model.PaymentMethod;
import com.smartdine.model.payment.CashPayment;
import com.smartdine.model.payment.Payment;
import com.smartdine.model.payment.PaymentResult;
import com.smartdine.model.payment.UPIPayment;
import com.smartdine.service.PaymentService;
import org.springframework.stereotype.Service;

/**
 * OOP POLYMORPHISM DEMONSTRATION:
 * The PaymentService dynamically instantiates the appropriate Payment implementation
 * (CashPayment or UPIPayment) and invokes processPayment() through the Payment interface.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public PaymentResult processOrderPayment(double amount, PaymentMethod method, String referenceDetails, Integer tableNumber) {
        if (amount <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero. Received: ₹" + amount);
        }

        // Polymorphic declaration
        Payment payment;

        if (method == PaymentMethod.CASH) {
            payment = new CashPayment("COUNTER-STAFF", tableNumber != null ? tableNumber.toString() : "DINE-IN");
        } else if (method == PaymentMethod.UPI) {
            payment = new UPIPayment(referenceDetails != null ? referenceDetails : "customer@upi");
        } else {
            throw new InvalidPaymentException("Unsupported payment method: " + method);
        }

        // Polymorphic invocation
        PaymentResult result = payment.processPayment(amount, referenceDetails);

        if (!result.isSuccessful()) {
            throw new InvalidPaymentException("Payment failed: " + result.getMessage());
        }

        return result;
    }
}
