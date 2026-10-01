package com.smartdine.service.impl;

import com.smartdine.dto.DashboardStatsDto;
import com.smartdine.exception.EstablishmentNotFoundException;
import com.smartdine.model.*;
import com.smartdine.repository.EstablishmentRepository;
import com.smartdine.repository.FoodItemRepository;
import com.smartdine.repository.OrderRepository;
import com.smartdine.repository.ReservationRepository;
import com.smartdine.service.EstablishmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EstablishmentServiceImpl implements EstablishmentService {

    private final EstablishmentRepository establishmentRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final FoodItemRepository foodItemRepository;

    public EstablishmentServiceImpl(EstablishmentRepository establishmentRepository,
                                   OrderRepository orderRepository,
                                   ReservationRepository reservationRepository,
                                   FoodItemRepository foodItemRepository) {
        this.establishmentRepository = establishmentRepository;
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public List<Establishment> getAllEstablishments() {
        return establishmentRepository.findByStatus(UserStatus.ACTIVE);
    }

    @Override
    public List<Establishment> getNearbyEstablishments(double userLat, double userLon, Double maxRadiusKm, EstablishmentType type) {
        double radius = (maxRadiusKm != null && maxRadiusKm > 0) ? maxRadiusKm : 25.0; // Default 25 km
        List<Establishment> all = (type != null)
                ? establishmentRepository.findByTypeAndStatus(type, UserStatus.ACTIVE)
                : establishmentRepository.findByStatus(UserStatus.ACTIVE);

        // Java Streams & OOP Collections: compute distance and filter
        return all.stream()
                .peek(e -> e.calculateDistance(userLat, userLon))
                .filter(e -> e.getDistanceKm() <= radius)
                .sorted(Comparator.comparingDouble(Establishment::getDistanceKm))
                .collect(Collectors.toList());
    }

    @Override
    public Establishment getEstablishmentById(Long establishmentId) {
        return establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new EstablishmentNotFoundException(establishmentId));
    }

    @Override
    public List<Establishment> getEstablishmentsByType(EstablishmentType type) {
        return establishmentRepository.findByTypeAndStatus(type, UserStatus.ACTIVE);
    }

    @Override
    public List<Establishment> searchEstablishments(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllEstablishments();
        }
        return establishmentRepository.searchEstablishments(query.trim(), UserStatus.ACTIVE);
    }

    @Override
    public List<Establishment> getEstablishmentsByAmbiance(AmbianceType ambiance) {
        if (ambiance == null || ambiance == AmbianceType.DEFAULT) {
            return getAllEstablishments();
        }
        return establishmentRepository.findByStatus(UserStatus.ACTIVE).stream()
                .filter(e -> e.getSupportedAmbiances().contains(ambiance))
                .collect(Collectors.toList());
    }

    @Override
    public Establishment saveEstablishment(Establishment establishment) {
        return establishmentRepository.save(establishment);
    }

    @Override
    public DashboardStatsDto getEstablishmentDashboardStats(Long establishmentId) {
        Establishment establishment = getEstablishmentById(establishmentId);
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setEstablishmentId(establishment.getEstablishmentId());
        stats.setEstablishmentName(establishment.getName());
        stats.setEstablishmentType(establishment.getType().name());
        stats.setCustomerRating(establishment.getRating() != null ? establishment.getRating() : 4.0);
        stats.setReviewCount(establishment.getReviewCount() != null ? establishment.getReviewCount() : 0);

        long totalOrders = orderRepository.countByEstablishment_EstablishmentId(establishmentId);
        stats.setTotalOrders(totalOrders);

        long pending = orderRepository.countByEstablishment_EstablishmentIdAndStatus(establishmentId, OrderStatus.PLACED) +
                       orderRepository.countByEstablishment_EstablishmentIdAndStatus(establishmentId, OrderStatus.ACCEPTED) +
                       orderRepository.countByEstablishment_EstablishmentIdAndStatus(establishmentId, OrderStatus.PREPARING) +
                       orderRepository.countByEstablishment_EstablishmentIdAndStatus(establishmentId, OrderStatus.READY);
        stats.setPendingOrders(pending);

        long completed = orderRepository.countByEstablishment_EstablishmentIdAndStatus(establishmentId, OrderStatus.COMPLETED);
        stats.setCompletedOrders(completed);

        long fastServe = orderRepository.countByEstablishment_EstablishmentIdAndIsFastServeTrue(establishmentId);
        stats.setActiveFastServeOrders(fastServe);

        long activeReservations = reservationRepository.countByEstablishment_EstablishmentIdAndStatus(establishmentId, ReservationStatus.CONFIRMED);
        stats.setActiveReservations(activeReservations);

        // Calculate Turnover (strictly completed & paid orders processed through SmartDine)
        Double totalTurnover = orderRepository.calculateEstablishmentTurnover(establishmentId, OrderStatus.COMPLETED, PaymentStatus.COMPLETED);
        stats.setTotalTurnover(totalTurnover != null ? Math.round(totalTurnover * 100.0) / 100.0 : 0.0);

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Double todayRev = orderRepository.calculateEstablishmentTurnoverSince(establishmentId, OrderStatus.COMPLETED, PaymentStatus.COMPLETED, startOfToday);
        stats.setTodayRevenue(todayRev != null ? Math.round(todayRev * 100.0) / 100.0 : 0.0);

        // Fetch orders placed today
        List<Order> orders = orderRepository.findByEstablishment_EstablishmentIdOrderByCreatedAtDesc(establishmentId);
        long todayCount = orders.stream()
                .filter(o -> o.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                .count();
        stats.setTodayOrders(todayCount);

        // Top food items calculation using Streams
        Map<String, Long> itemOrderCounts = new HashMap<>();
        for (Order o : orders) {
            for (OrderItem oi : o.getItems()) {
                itemOrderCounts.put(oi.getFoodName(), itemOrderCounts.getOrDefault(oi.getFoodName(), 0L) + oi.getQuantity());
            }
        }

        List<Map<String, Object>> topList = itemOrderCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getKey());
                    map.put("orderCount", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        stats.setTopFoodItems(topList);

        // Least ordered food items
        List<Map<String, Object>> leastList = itemOrderCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(4)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getKey());
                    map.put("orderCount", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        stats.setLeastOrderedFoodItems(leastList);

        return stats;
    }
}
