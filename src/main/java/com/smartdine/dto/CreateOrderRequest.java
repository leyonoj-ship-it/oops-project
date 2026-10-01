package com.smartdine.dto;

import com.smartdine.model.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    private Boolean isFastServe = false;

    private Integer tableNumber;

    @NotNull(message = "Payment method is required (CASH or UPI)")
    private PaymentMethod paymentMethod = PaymentMethod.UPI;

    private String paymentReference; // VPA if UPI, or cashier note if Cash

    @NotEmpty(message = "Order must contain at least one food item")
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }

    public Boolean getIsFastServe() {
        return isFastServe;
    }

    public void setIsFastServe(Boolean fastServe) {
        isFastServe = fastServe;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
