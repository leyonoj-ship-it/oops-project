package com.smartdine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * OOP ENCAPSULATION & DATA INTEGRITY:
 * Represents food/beverage items offered by an Establishment (Hotel or Canteen).
 * Enforces dynamic price resolution (Normal vs Offer vs Clearance price) and
 * tags for the Ambiance Recommendation Engine.
 */
@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonIgnore
    private Establishment establishment;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private FoodCategory category;

    @Column(nullable = false)
    private Double price;

    @Column(name = "offer_price")
    private Double offerPrice;

    @Column(name = "clearance_price")
    private Double clearancePrice;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_veg")
    private Boolean isVeg = true;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 50;

    // OOP COLLECTIONS: Food item ambiance & dietary tags
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_tags", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public FoodItem() {
    }

    public FoodItem(String name, String description, Double price, Boolean isVeg, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isVeg = isVeg;
        this.imageUrl = imageUrl;
        this.isAvailable = true;
        this.stockQuantity = 50;
    }

    // Dynamic Price Calculation: Never trust frontend price!
    // Priority: Clearance Price > Offer Price > Regular Price
    public double getEffectivePrice() {
        if (clearancePrice != null && clearancePrice > 0) {
            return clearancePrice;
        }
        if (offerPrice != null && offerPrice > 0 && offerPrice < price) {
            return offerPrice;
        }
        return price;
    }

    public boolean hasOffer() {
        return (offerPrice != null && offerPrice > 0 && offerPrice < price) ||
               (clearancePrice != null && clearancePrice > 0 && clearancePrice < price);
    }

    public int getDiscountPercentage() {
        double effective = getEffectivePrice();
        if (effective < price && price > 0) {
            return (int) Math.round(((price - effective) / price) * 100);
        }
        return 0;
    }

    public boolean matchesAmbiance(AmbianceType ambiance) {
        if (ambiance == null || ambiance == AmbianceType.DEFAULT) return true;
        return tags.contains(ambiance.name());
    }

    // Decrement stock safely
    public boolean decrementStock(int quantity) {
        if (this.stockQuantity != null && this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            if (this.stockQuantity == 0) {
                this.isAvailable = false;
            }
            return true;
        }
        return false;
    }

    // Getters and Setters
    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public Double getClearancePrice() {
        return clearancePrice;
    }

    public void setClearancePrice(Double clearancePrice) {
        this.clearancePrice = clearancePrice;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Boolean getIsVeg() {
        return isVeg;
    }

    public void setIsVeg(Boolean isVeg) {
        this.isVeg = isVeg;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        if (stockQuantity != null && stockQuantity <= 0) {
            this.isAvailable = false;
        }
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
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
        if (!(o instanceof FoodItem foodItem)) return false;
        return Objects.equals(foodId, foodItem.foodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId);
    }

    @Override
    public String toString() {
        return name + " (₹" + getEffectivePrice() + ")";
    }
}
