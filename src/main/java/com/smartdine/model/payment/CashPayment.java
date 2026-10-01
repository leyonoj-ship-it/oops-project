package com.smartdine.model.payment;

import com.smartdine.model.PaymentMethod;
import com.smartdine.model.PaymentStatus;
import java.util.UUID;

/**
 * OOP POLYMORPHISM:
 * Cash implementation of the Payment interface.
 * Handles counter/table cash collections and generates physical receipt references.
 */
public class CashPayment implements Payment {

    private String cashierId;
    private String tableNumber;

    public CashPayment() {
    }

    public CashPayment(String cashierId, String tableNumber) {
        this.cashierId = cashierId;
        this.tableNumber = tableNumber;
    }

    @Override
    public PaymentResult processPayment(double amount, String referenceDetails) {
        if (!validateAmount(amount)) {
            return new PaymentResult(false, null, PaymentMethod.CASH,
                    PaymentStatus.FAILED, amount, "Invalid cash billing amount: ₹" + amount);
        }

        String receiptNo = "CASH-REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String note = referenceDetails != null ? referenceDetails : "Pay at counter/table";

        PaymentResult result = new PaymentResult(
                true,
                receiptNo,
                PaymentMethod.CASH,
                PaymentStatus.COMPLETED,
                amount,
                "Cash payment recorded successfully. Receipt #" + receiptNo + " generated."
        );
        result.setReceiptDetails("Table: " + (tableNumber != null ? tableNumber : "Counter") + " | " + note);
        return result;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH;
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }
}
