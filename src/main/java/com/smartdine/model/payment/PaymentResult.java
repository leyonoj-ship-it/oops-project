package com.smartdine.model.payment;

import com.smartdine.model.PaymentMethod;
import com.smartdine.model.PaymentStatus;
import java.time.LocalDateTime;

/**
 * Result object returned by polymorphic Payment implementations.
 */
public class PaymentResult {
    private boolean successful;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private double amount;
    private String message;
    private String receiptDetails;
    private LocalDateTime timestamp;

    public PaymentResult(boolean successful, String transactionId, PaymentMethod paymentMethod,
                         PaymentStatus status, double amount, String message) {
        this.successful = successful;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.amount = amount;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiptDetails() {
        return receiptDetails;
    }

    public void setReceiptDetails(String receiptDetails) {
        this.receiptDetails = receiptDetails;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
