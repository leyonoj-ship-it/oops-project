package com.smartdine.controller;

import com.smartdine.dto.CreateOrderRequest;
import com.smartdine.model.Order;
import com.smartdine.model.OrderStatus;
import com.smartdine.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(originPatterns = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.placeOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<Order>> getOrdersByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(orderService.getOrdersByEstablishment(establishmentId));
    }

    @GetMapping("/establishment/{establishmentId}/fast-serve")
    public ResponseEntity<List<Order>> getFastServeOrders(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(orderService.getFastServeOrdersByEstablishment(establishmentId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id, @RequestParam(defaultValue = "Cancelled by user") String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(id, reason));
    }
}
