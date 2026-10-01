package com.smartdine.service.impl;

import com.smartdine.dto.DashboardStatsDto;
import com.smartdine.exception.EstablishmentNotFoundException;
import com.smartdine.exception.UserNotFoundException;
import com.smartdine.model.*;
import com.smartdine.repository.*;
import com.smartdine.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final PersonRepository personRepository;
    private final EstablishmentRepository establishmentRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public AdminServiceImpl(PersonRepository personRepository,
                            EstablishmentRepository establishmentRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository) {
        this.personRepository = personRepository;
        this.establishmentRepository = establishmentRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getPlatformAnalytics(String timeRange) {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalUsers(personRepository.count());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newUsers = personRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
        stats.setNewUsers(newUsers);

        stats.setTotalEstablishments(establishmentRepository.count());
        stats.setActiveEstablishments(establishmentRepository.countByStatus(UserStatus.ACTIVE));
        stats.setTotalHotels(establishmentRepository.countByType(EstablishmentType.HOTEL));
        stats.setTotalCanteens(establishmentRepository.countByType(EstablishmentType.CANTEEN));

        stats.setTotalOrders(orderRepository.count());
        stats.setCompletedOrders(orderRepository.countByStatus(OrderStatus.COMPLETED));
        stats.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));

        // Strict calculation of turnover: ONLY completed and paid orders!
        Double allTimeTurnover = orderRepository.calculateTotalTurnover(OrderStatus.COMPLETED, PaymentStatus.COMPLETED);
        stats.setTotalTurnoverThroughWeb(allTimeTurnover != null ? Math.round(allTimeTurnover * 100.0) / 100.0 : 0.0);

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Double todayTurnover = orderRepository.calculateTurnoverSince(OrderStatus.COMPLETED, PaymentStatus.COMPLETED, startOfToday);
        stats.setTodayTurnover(todayTurnover != null ? Math.round(todayTurnover * 100.0) / 100.0 : 0.0);

        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        Double weekTurnover = orderRepository.calculateTurnoverSince(OrderStatus.COMPLETED, PaymentStatus.COMPLETED, startOfWeek);
        stats.setWeekTurnover(weekTurnover != null ? Math.round(weekTurnover * 100.0) / 100.0 : 0.0);

        LocalDateTime startOfMonth = LocalDate.now().minusDays(30).atStartOfDay();
        Double monthTurnover = orderRepository.calculateTurnoverSince(OrderStatus.COMPLETED, PaymentStatus.COMPLETED, startOfMonth);
        stats.setMonthTurnover(monthTurnover != null ? Math.round(monthTurnover * 100.0) / 100.0 : 0.0);

        // Top Establishments by completed turnover
        List<Establishment> establishments = establishmentRepository.findAll();
        List<Map<String, Object>> topEstList = establishments.stream().map(est -> {
            Double turnover = orderRepository.calculateEstablishmentTurnover(est.getEstablishmentId(), OrderStatus.COMPLETED, PaymentStatus.COMPLETED);
            long orders = orderRepository.countByEstablishment_EstablishmentId(est.getEstablishmentId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", est.getEstablishmentId());
            map.put("name", est.getName());
            map.put("type", est.getType().name());
            map.put("rating", est.getRating());
            map.put("orderCount", orders);
            map.put("turnover", turnover != null ? Math.round(turnover * 100.0) / 100.0 : 0.0);
            return map;
        }).sorted((a, b) -> Double.compare((Double) b.get("turnover"), (Double) a.get("turnover")))
          .limit(5)
          .collect(Collectors.toList());

        stats.setTopEstablishments(topEstList);

        // Recent reviews
        List<Review> reviews = reviewRepository.findAll();
        List<Map<String, Object>> recentReviews = reviews.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getReviewId());
                    map.put("customerName", r.getCustomer().getName());
                    map.put("establishmentName", r.getEstablishment().getName());
                    map.put("rating", r.getRating());
                    map.put("comment", r.getComment());
                    map.put("flagged", r.getIsFlagged());
                    map.put("date", r.getCreatedAt().toLocalDate().toString());
                    return map;
                })
                .collect(Collectors.toList());
        stats.setRecentReviews(recentReviews);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> getAllUsers(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return personRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim());
        }
        return personRepository.findAll();
    }

    @Override
    public Person updateUserStatus(Long userId, UserStatus status) {
        Person person = personRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        person.setStatus(status);
        return personRepository.save(person);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Establishment> getAllEstablishments() {
        return establishmentRepository.findAll();
    }

    @Override
    public Establishment updateEstablishmentStatus(Long establishmentId, UserStatus status) {
        Establishment establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new EstablishmentNotFoundException(establishmentId));
        establishment.setStatus(status);
        return establishmentRepository.save(establishment);
    }
}
