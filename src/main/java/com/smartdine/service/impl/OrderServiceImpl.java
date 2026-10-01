package com.smartdine.service.impl;

import com.smartdine.dto.CreateOrderRequest;
import com.smartdine.dto.OrderItemRequest;
import com.smartdine.exception.*;
import com.smartdine.model.*;
import com.smartdine.model.payment.PaymentResult;
import com.smartdine.repository.*;
import com.smartdine.service.OrderService;
import com.smartdine.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final EstablishmentRepository establishmentRepository;
    private final FoodItemRepository foodItemRepository;
    private final DiningTableRepository diningTableRepository;
    private final ClearanceOfferRepository clearanceOfferRepository;
    private final PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            EstablishmentRepository establishmentRepository,
                            FoodItemRepository foodItemRepository,
                            DiningTableRepository diningTableRepository,
                            ClearanceOfferRepository clearanceOfferRepository,
                            PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.establishmentRepository = establishmentRepository;
        this.foodItemRepository = foodItemRepository;
        this.diningTableRepository = diningTableRepository;
        this.clearanceOfferRepository = clearanceOfferRepository;
        this.paymentService = paymentService;
    }

    @Override
    public Order placeOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new UserNotFoundException(request.getCustomerId()));

        Establishment establishment = establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(request.getEstablishmentId()));

        if (establishment.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOrderException("Establishment is not active for ordering.");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Cannot place an empty order. Select at least one item.");
        }

        // Generate human-friendly order number: e.g. SD-83921
        String orderNumber = "SD-" + (System.currentTimeMillis() % 1000000);

        Order order = new Order(orderNumber, customer, establishment);
        order.setIsFastServe(Boolean.TRUE.equals(request.getIsFastServe()));
        order.setPaymentMethod(request.getPaymentMethod());

        // Fast Serve table linkage
        if (Boolean.TRUE.equals(request.getIsFastServe())) {
            if (request.getTableNumber() == null || request.getTableNumber() <= 0) {
                throw new InvalidOrderException("Fast Serve requires a valid Table Number.");
            }
            order.setTableNumber(request.getTableNumber());
            diningTableRepository.findByEstablishment_EstablishmentIdAndTableNumber(establishment.getEstablishmentId(), request.getTableNumber())
                    .ifPresent(order::setTable);
        }

        // Process order line items with backend pricing and inventory verification
        for (OrderItemRequest itemReq : request.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemReq.getFoodId())
                    .orElseThrow(() -> new FoodNotAvailableException(itemReq.getFoodId(), "Item #" + itemReq.getFoodId()));

            if (!Boolean.TRUE.equals(foodItem.getIsAvailable())) {
                throw new FoodNotAvailableException(foodItem.getFoodId(), foodItem.getName());
            }

            if (foodItem.getStockQuantity() != null && foodItem.getStockQuantity() < itemReq.getQuantity()) {
                throw new InsufficientFoodQuantityException(foodItem.getName(), itemReq.getQuantity(), foodItem.getStockQuantity());
            }

            // Decrement item inventory
            foodItem.decrementStock(itemReq.getQuantity());

            // If clearance offer applies, deduct from clearance remaining portions
            List<ClearanceOffer> clearances = clearanceOfferRepository.findByEstablishment_EstablishmentIdAndIsActiveTrue(establishment.getEstablishmentId());
            for (ClearanceOffer co : clearances) {
                if (co.getFoodItem().getFoodId().equals(foodItem.getFoodId())) {
                    co.deductClearancePortion(itemReq.getQuantity());
                    clearanceOfferRepository.save(co);
                    break;
                }
            }

            foodItemRepository.save(foodItem);

            OrderItem orderItem = new OrderItem(foodItem, itemReq.getQuantity(), itemReq.getCustomization());
            order.addItem(orderItem);
        }

        // Calculate final total (Subtotal - discounts + GST tax)
        order.calculateTotals();

        // Process Payment polymorphically (Cash or UPI)
        PaymentResult paymentResult = paymentService.processOrderPayment(
                order.getFinalAmount(),
                request.getPaymentMethod(),
                request.getPaymentReference(),
                order.getTableNumber()
        );

        order.setPaymentStatus(paymentResult.getStatus());
        order.setPaymentReference(paymentResult.getTransactionId());
        order.setStatus(OrderStatus.PLACED);

        // Award customer loyalty points (1 point per ₹100 spent)
        customer.addLoyaltyPoints((int) (order.getFinalAmount() / 100));
        customerRepository.save(customer);

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order not found with ID: " + orderId));
    }

    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new InvalidOrderException("Order not found with number: " + orderNumber));
    }

    @Override
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<Order> getOrdersByEstablishment(Long establishmentId) {
        return orderRepository.findByEstablishment_EstablishmentIdOrderByCreatedAtDesc(establishmentId);
    }

    @Override
    public List<Order> getFastServeOrdersByEstablishment(Long establishmentId) {
        return orderRepository.findByEstablishment_EstablishmentIdAndIsFastServeTrueOrderByCreatedAtDesc(establishmentId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        if (!order.canTransitionTo(newStatus)) {
            throw new InvalidOrderException("Invalid order status transition from " + order.getStatus() + " to " + newStatus);
        }
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.SERVED) {
            throw new InvalidOrderException("Cannot cancel an order that has already been served or completed.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
