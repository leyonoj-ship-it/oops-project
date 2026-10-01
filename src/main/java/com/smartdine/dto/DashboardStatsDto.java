package com.smartdine.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    // Establishment-specific metrics
    private Long establishmentId;
    private String establishmentName;
    private String establishmentType;
    private long totalOrders;
    private long todayOrders;
    private long pendingOrders;
    private long completedOrders;
    private double totalTurnover; // Through SmartDine
    private double todayRevenue;
    private double customerRating;
    private long reviewCount;
    private long activeReservations;
    private long activeFastServeOrders;

    // Platform-wide / Admin metrics
    private long totalUsers;
    private long newUsers;
    private long totalEstablishments;
    private long activeEstablishments;
    private long totalHotels;
    private long totalCanteens;
    private long cancelledOrders;
    private double totalTurnoverThroughWeb; // Completed & Paid only
    private double todayTurnover;
    private double weekTurnover;
    private double monthTurnover;

    // Charts & Top Items
    private List<Map<String, Object>> topFoodItems;
    private List<Map<String, Object>> leastOrderedFoodItems;
    private List<Map<String, Object>> recentReviews;
    private List<Map<String, Object>> topEstablishments;

    public DashboardStatsDto() {
    }

    public Long getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }

    public String getEstablishmentName() {
        return establishmentName;
    }

    public void setEstablishmentName(String establishmentName) {
        this.establishmentName = establishmentName;
    }

    public String getEstablishmentType() {
        return establishmentType;
    }

    public void setEstablishmentType(String establishmentType) {
        this.establishmentType = establishmentType;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(long todayOrders) {
        this.todayOrders = todayOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public double getTotalTurnover() {
        return totalTurnover;
    }

    public void setTotalTurnover(double totalTurnover) {
        this.totalTurnover = totalTurnover;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public double getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(double customerRating) {
        this.customerRating = customerRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public long getActiveReservations() {
        return activeReservations;
    }

    public void setActiveReservations(long activeReservations) {
        this.activeReservations = activeReservations;
    }

    public long getActiveFastServeOrders() {
        return activeFastServeOrders;
    }

    public void setActiveFastServeOrders(long activeFastServeOrders) {
        this.activeFastServeOrders = activeFastServeOrders;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(long newUsers) {
        this.newUsers = newUsers;
    }

    public long getTotalEstablishments() {
        return totalEstablishments;
    }

    public void setTotalEstablishments(long totalEstablishments) {
        this.totalEstablishments = totalEstablishments;
    }

    public long getActiveEstablishments() {
        return activeEstablishments;
    }

    public void setActiveEstablishments(long activeEstablishments) {
        this.activeEstablishments = activeEstablishments;
    }

    public long getTotalHotels() {
        return totalHotels;
    }

    public void setTotalHotels(long totalHotels) {
        this.totalHotels = totalHotels;
    }

    public long getTotalCanteens() {
        return totalCanteens;
    }

    public void setTotalCanteens(long totalCanteens) {
        this.totalCanteens = totalCanteens;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public double getTotalTurnoverThroughWeb() {
        return totalTurnoverThroughWeb;
    }

    public void setTotalTurnoverThroughWeb(double totalTurnoverThroughWeb) {
        this.totalTurnoverThroughWeb = totalTurnoverThroughWeb;
    }

    public double getTodayTurnover() {
        return todayTurnover;
    }

    public void setTodayTurnover(double todayTurnover) {
        this.todayTurnover = todayTurnover;
    }

    public double getWeekTurnover() {
        return weekTurnover;
    }

    public void setWeekTurnover(double weekTurnover) {
        this.weekTurnover = weekTurnover;
    }

    public double getMonthTurnover() {
        return monthTurnover;
    }

    public void setMonthTurnover(double monthTurnover) {
        this.monthTurnover = monthTurnover;
    }

    public List<Map<String, Object>> getTopFoodItems() {
        return topFoodItems;
    }

    public void setTopFoodItems(List<Map<String, Object>> topFoodItems) {
        this.topFoodItems = topFoodItems;
    }

    public List<Map<String, Object>> getLeastOrderedFoodItems() {
        return leastOrderedFoodItems;
    }

    public void setLeastOrderedFoodItems(List<Map<String, Object>> leastOrderedFoodItems) {
        this.leastOrderedFoodItems = leastOrderedFoodItems;
    }

    public List<Map<String, Object>> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<Map<String, Object>> recentReviews) {
        this.recentReviews = recentReviews;
    }

    public List<Map<String, Object>> getTopEstablishments() {
        return topEstablishments;
    }

    public void setTopEstablishments(List<Map<String, Object>> topEstablishments) {
        this.topEstablishments = topEstablishments;
    }
}
