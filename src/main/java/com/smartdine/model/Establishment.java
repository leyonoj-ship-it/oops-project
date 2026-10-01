package com.smartdine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * OOP ARCHITECTURAL HIGHLIGHT:
 * CRITICAL REQUIREMENT: Hotel and Canteen are NOT separate classes.
 * Both belong to this single Establishment class, differentiated by EstablishmentType (HOTEL / CANTEEN).
 * This eliminates redundant code, unifies food discovery, table reservation, fast serve,
 * and management logic while adhering to Clean OOP Design and Composition.
 */
@Entity
@Table(name = "establishments")
public class Establishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "establishment_id")
    private Long establishmentId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstablishmentType type; // HOTEL or CANTEEN

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "location_address", nullable = false)
    private String locationAddress;

    @Column(length = 100)
    private String city = "Bengaluru";

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 20)
    private String phone;

    private Double rating = 4.2;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "opening_time", length = 10)
    private String openingTime = "08:00";

    @Column(name = "closing_time", length = 10)
    private String closingTime = "22:30";

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private EstablishmentOwner owner;

    // OOP COMPOSITION: Supported Ambiance Types
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "establishment_ambiances", joinColumns = @JoinColumn(name = "establishment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "ambiance_type")
    private Set<AmbianceType> supportedAmbiances = new HashSet<>();

    // OOP COMPOSITION: Establishment has-a Menu (List of FoodItems)
    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FoodItem> menu = new ArrayList<>();

    // OOP COMPOSITION: Establishment has-a list of DiningTables
    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DiningTable> tables = new ArrayList<>();

    // OOP COMPOSITION: Clearance Offers
    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ClearanceOffer> clearanceOffers = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Transient field for runtime distance calculation
    @Transient
    private Double distanceKm;

    public Establishment() {
    }

    // Overloaded Constructor
    public Establishment(String name, EstablishmentType type, String address, Double latitude, Double longitude) {
        this.name = name;
        this.type = type;
        this.locationAddress = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Full Parameterized Constructor
    public Establishment(String name, EstablishmentType type, String description, String locationAddress,
                         String city, Double latitude, Double longitude, String phone,
                         String openingTime, String closingTime, String coverImageUrl) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.locationAddress = locationAddress;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.coverImageUrl = coverImageUrl;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Business Method: Calculate Haversine Distance
    public double calculateDistance(double userLat, double userLon) {
        final int EARTH_RADIUS_KM = 6371;
        double latDistance = Math.toRadians(this.latitude - userLat);
        double lonDistance = Math.toRadians(this.longitude - userLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(this.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        this.distanceKm = Math.round((EARTH_RADIUS_KM * c) * 10.0) / 10.0;
        return this.distanceKm;
    }

    // Business Method: Check if establishment is currently open
    public boolean isOpenNow() {
        try {
            LocalTime now = LocalTime.now();
            LocalTime open = LocalTime.parse(this.openingTime);
            LocalTime close = LocalTime.parse(this.closingTime);
            if (close.isBefore(open)) {
                // Crosses midnight
                return now.isAfter(open) || now.isBefore(close);
            }
            return now.isAfter(open) && now.isBefore(close);
        } catch (Exception e) {
            return true; // Default fallback
        }
    }

    // Helper methods for bidirectional relationships (Composition management)
    public void addFoodItem(FoodItem item) {
        menu.add(item);
        item.setEstablishment(this);
    }

    public void removeFoodItem(FoodItem item) {
        menu.remove(item);
        item.setEstablishment(null);
    }

    public void addTable(DiningTable table) {
        tables.add(table);
        table.setEstablishment(this);
    }

    public void updateRating(double newRating) {
        if (this.reviewCount == null || this.reviewCount == 0) {
            this.rating = newRating;
            this.reviewCount = 1;
        } else {
            double total = (this.rating * this.reviewCount) + newRating;
            this.reviewCount++;
            this.rating = Math.round((total / this.reviewCount) * 10.0) / 10.0;
        }
    }

    // Getters and Setters
    public Long getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EstablishmentType getType() {
        return type;
    }

    public void setType(EstablishmentType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public EstablishmentOwner getOwner() {
        return owner;
    }

    public void setOwner(EstablishmentOwner owner) {
        this.owner = owner;
    }

    public Set<AmbianceType> getSupportedAmbiances() {
        return supportedAmbiances;
    }

    public void setSupportedAmbiances(Set<AmbianceType> supportedAmbiances) {
        this.supportedAmbiances = supportedAmbiances;
    }

    public List<FoodItem> getMenu() {
        return menu;
    }

    public void setMenu(List<FoodItem> menu) {
        this.menu = menu;
    }

    public List<DiningTable> getTables() {
        return tables;
    }

    public void setTables(List<DiningTable> tables) {
        this.tables = tables;
    }

    public List<ClearanceOffer> getClearanceOffers() {
        return clearanceOffers;
    }

    public void setClearanceOffers(List<ClearanceOffer> clearanceOffers) {
        this.clearanceOffers = clearanceOffers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Establishment that)) return false;
        return Objects.equals(establishmentId, that.establishmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(establishmentId);
    }

    @Override
    public String toString() {
        return "Establishment{" +
                "id=" + establishmentId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", city='" + city + '\'' +
                ", rating=" + rating +
                '}';
    }
}
