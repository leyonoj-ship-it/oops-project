package com.smartdine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OOP COMPOSITION & LIFECYCLE MANAGEMENT:
 * Order represents a dining/takeaway or Fast Serve transaction.
 * Composes Customer, Establishment, DiningTable, and a Collection of OrderItems.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonIgnoreProperties({"menu", "tables", "clearanceOffers", "hibernateLazyInitializer", "handler"})
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @Column(name = "table_number")
    private Integer tableNumber;

    // FAST SERVE FLAG: CRITICAL SMARTDINE FEATURE
    @Column(name = "is_fast_serve")
    private Boolean isFastServe = false;

    // OOP COMPOSITION: Order has-a List of OrderItems
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Double subtotal = 0.0;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "tax_amount")
    private Double taxAmount = 0.0;

    @Column(name = "final_amount", nullable = false)
    private Double finalAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.UPI;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {
    }

    public Order(String orderNumber, Customer customer, Establishment establishment) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.establishment = establishment;
        this.status = OrderStatus.PLACED;
        this.paymentStatus = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Fast Serve constructor
    public Order(String orderNumber, Customer customer, Establishment establishment, int tableNumber) {
        this(orderNumber, customer, establishment);
        this.tableNumber = tableNumber;
        this.isFastServe = true;
    }

    // Bidirectional composition helper
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }

    // Business Logic: Price & Tax calculation (5% GST standard dining tax)
    public void calculateTotals() {
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.getTotalPrice();
        }
        this.subtotal = Math.round(sum * 100.0) / 100.0;
        if (this.discountAmount == null) this.discountAmount = 0.0;
        double taxable = Math.max(0.0, this.subtotal - this.discountAmount);
        this.taxAmount = Math.round((taxable * 0.05) * 100.0) / 100.0; // 5% GST
        this.finalAmount = Math.round((taxable + this.taxAmount) * 100.0) / 100.0;
    }

    // Business Logic: Order Status transitions
    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.COMPLETED) {
            return false;
        }
        return switch (this.status) {
            case PLACED -> nextStatus == OrderStatus.ACCEPTED || nextStatus == OrderStatus.CANCELLED;
            case ACCEPTED -> nextStatus == OrderStatus.PREPARING || nextStatus == OrderStatus.CANCELLED;
            case PREPARING -> nextStatus == OrderStatus.READY;
            case READY -> nextStatus == OrderStatus.SERVED;
            case SERVED -> nextStatus == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public DiningTable getTable() {
        return table;
    }

    public void setTable(DiningTable table) {
        this.table = table;
        if (table != null) {
            this.tableNumber = table.getTableNumber();
        }
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Boolean getIsFastServe() {
        return isFastServe;
    }

    public void setIsFastServe(Boolean fastServe) {
        isFastServe = fastServe;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotals();
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotals();
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
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
        if (!(o instanceof Order order)) return false;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return (isFastServe ? "[FAST SERVE] " : "") + "Order #" + orderNumber +
                " (Table " + tableNumber + ") - ₹" + finalAmount + " [" + status + "]";
    }
}
