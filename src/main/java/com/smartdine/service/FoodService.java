package com.smartdine.service;

import com.smartdine.dto.ClearanceOfferRequest;
import com.smartdine.dto.FoodItemDto;
import com.smartdine.model.ClearanceOffer;
import com.smartdine.model.FoodCategory;
import com.smartdine.model.FoodItem;

import java.util.List;

public interface FoodService {
    List<FoodItem> getMenuByEstablishment(Long establishmentId);
    FoodItem getFoodItemById(Long foodId);
    FoodItem addFoodItem(FoodItemDto dto);
    FoodItem updateFoodItem(Long foodId, FoodItemDto dto);
    void deleteFoodItem(Long foodId);
    FoodItem toggleAvailability(Long foodId);
    
    // Clearance offers (Closing time smart discount)
    ClearanceOffer createClearanceOffer(ClearanceOfferRequest request);
    List<ClearanceOffer> getActiveClearanceOffers(Long establishmentId);
    List<ClearanceOffer> getAllActiveClearanceOffers();
    void expireClearanceOffer(Long offerId);

    List<FoodCategory> getAllCategories();
}
