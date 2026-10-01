package com.smartdine.controller;

import com.smartdine.dto.ClearanceOfferRequest;
import com.smartdine.dto.FoodItemDto;
import com.smartdine.model.AmbianceType;
import com.smartdine.model.ClearanceOffer;
import com.smartdine.model.FoodCategory;
import com.smartdine.model.FoodItem;
import com.smartdine.service.FoodService;
import com.smartdine.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(originPatterns = "*")
public class FoodController {

    private final FoodService foodService;
    private final RecommendationService recommendationService;

    public FoodController(FoodService foodService, RecommendationService recommendationService) {
        this.foodService = foodService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<FoodItem>> getMenuByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(foodService.getMenuByEstablishment(establishmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodItem> getFoodItem(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodItemById(id));
    }

    @PostMapping
    public ResponseEntity<FoodItem> addFoodItem(@Valid @RequestBody FoodItemDto dto) {
        FoodItem created = foodService.addFoodItem(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodItem> updateFoodItem(@PathVariable Long id, @Valid @RequestBody FoodItemDto dto) {
        FoodItem updated = foodService.updateFoodItem(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        foodService.deleteFoodItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<FoodItem> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.toggleAvailability(id));
    }

    // SMART FOOD CLEARANCE ENDPOINTS
    @PostMapping("/clearance-offers")
    public ResponseEntity<ClearanceOffer> createClearanceOffer(@Valid @RequestBody ClearanceOfferRequest request) {
        ClearanceOffer offer = foodService.createClearanceOffer(request);
        return new ResponseEntity<>(offer, HttpStatus.CREATED);
    }

    @GetMapping("/clearance-offers/active")
    public ResponseEntity<List<ClearanceOffer>> getAllActiveClearanceOffers() {
        return ResponseEntity.ok(foodService.getAllActiveClearanceOffers());
    }

    @GetMapping("/clearance-offers/establishment/{establishmentId}")
    public ResponseEntity<List<ClearanceOffer>> getEstablishmentClearanceOffers(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(foodService.getActiveClearanceOffers(establishmentId));
    }

    @PostMapping("/clearance-offers/{id}/expire")
    public ResponseEntity<Void> expireClearance(@PathVariable Long id) {
        foodService.expireClearanceOffer(id);
        return ResponseEntity.ok().build();
    }

    // AMBIANCE RECOMMENDATION SYSTEM
    @GetMapping("/recommendations")
    public ResponseEntity<List<FoodItem>> getRecommendations(
            @RequestParam(required = false) AmbianceType ambiance,
            @RequestParam(required = false) Long establishmentId) {
        return ResponseEntity.ok(recommendationService.getRecommendedFoodForAmbiance(ambiance, establishmentId));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<FoodCategory>> getCategories() {
        return ResponseEntity.ok(foodService.getAllCategories());
    }
}
