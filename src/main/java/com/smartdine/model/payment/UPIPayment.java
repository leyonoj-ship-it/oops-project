package com.smartdine.model.payment;

import com.smartdine.model.PaymentMethod;
import com.smartdine.model.PaymentStatus;
import java.util.UUID;

/**
 * OOP POLYMORPHISM:
 * UPI implementation of the Payment interface.
 * Simulates real-time UPI payments, validates Virtual Payment Addresses (VPA),
 * and generates verifiable transaction references.
 */
public class UPIPayment implements Payment {

    private String vpa;

    public UPIPayment() {
    }

    public UPIPayment(String vpa) {
        this.vpa = vpa;
    }

    @Override
    public PaymentResult processPayment(double amount, String referenceDetails) {
        if (!validateAmount(amount)) {
            return new PaymentResult(false, null, PaymentMethod.UPI,
                    PaymentStatus.FAILED, amount, "Invalid UPI transaction amount: ₹" + amount);
        }

        String userVpa = (referenceDetails != null && referenceDetails.contains("@"))
                ? referenceDetails
                : (this.vpa != null ? this.vpa : "customer@smartdine.upi");

        // Generate authentic UPI simulated reference
        String upiTxnId = "UPI-TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String qrPayload = "upi://pay?pa=smartdine.pay@bank&pn=SmartDine&am=" + amount + "&tr=" + upiTxnId + "&cu=INR";

        PaymentResult result = new PaymentResult(
                true,
                upiTxnId,
                PaymentMethod.UPI,
                PaymentStatus.COMPLETED,
                amount,
                "UPI payment of ₹" + amount + " successfully processed via " + userVpa
        );
        result.setReceiptDetails("UPI Reference: " + upiTxnId + " | Payload: " + qrPayload);
        return result;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.UPI;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }
}
