package com.smartdine.service;

import com.smartdine.dto.CreateOrderRequest;
import com.smartdine.model.Order;
import com.smartdine.model.OrderStatus;

import java.util.List;

public interface OrderService {
    Order placeOrder(CreateOrderRequest request);
    Order getOrderById(Long orderId);
    Order getOrderByOrderNumber(String orderNumber);
    List<Order> getOrdersByCustomer(Long customerId);
    List<Order> getOrdersByEstablishment(Long establishmentId);
    List<Order> getFastServeOrdersByEstablishment(Long establishmentId);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    Order cancelOrder(Long orderId, String reason);
}
