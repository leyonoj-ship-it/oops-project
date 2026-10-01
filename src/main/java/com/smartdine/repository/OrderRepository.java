package com.smartdine.repository;

import com.smartdine.model.Order;
import com.smartdine.model.OrderStatus;
import com.smartdine.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByEstablishment_EstablishmentIdOrderByCreatedAtDesc(Long establishmentId);

    List<Order> findByEstablishment_EstablishmentIdAndStatus(Long establishmentId, OrderStatus status);

    List<Order> findByEstablishment_EstablishmentIdAndIsFastServeTrueOrderByCreatedAtDesc(Long establishmentId);

    List<Order> findByStatus(OrderStatus status);

    long countByStatus(OrderStatus status);

    long countByEstablishment_EstablishmentId(Long establishmentId);

    long countByEstablishment_EstablishmentIdAndStatus(Long establishmentId, OrderStatus status);

    long countByEstablishment_EstablishmentIdAndIsFastServeTrue(Long establishmentId);

    // Platform-wide turnover: Completed orders only
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0.0) FROM Order o WHERE o.status = :status AND o.paymentStatus = :paymentStatus")
    Double calculateTotalTurnover(@Param("status") OrderStatus status, @Param("paymentStatus") PaymentStatus paymentStatus);

    // Turnover filtered by date range
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0.0) FROM Order o WHERE o.status = :status AND o.paymentStatus = :paymentStatus AND o.createdAt >= :startDate")
    Double calculateTurnoverSince(@Param("status") OrderStatus status,
                                 @Param("paymentStatus") PaymentStatus paymentStatus,
                                 @Param("startDate") LocalDateTime startDate);

    // Establishment-specific turnover
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0.0) FROM Order o WHERE o.establishment.establishmentId = :establishmentId AND o.status = :status AND o.paymentStatus = :paymentStatus")
    Double calculateEstablishmentTurnover(@Param("establishmentId") Long establishmentId,
                                         @Param("status") OrderStatus status,
                                         @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0.0) FROM Order o WHERE o.establishment.establishmentId = :establishmentId AND o.status = :status AND o.paymentStatus = :paymentStatus AND o.createdAt >= :startDate")
    Double calculateEstablishmentTurnoverSince(@Param("establishmentId") Long establishmentId,
                                              @Param("status") OrderStatus status,
                                              @Param("paymentStatus") PaymentStatus paymentStatus,
                                              @Param("startDate") LocalDateTime startDate);
}
