package com.smartdine.service.impl;

import com.smartdine.dto.ClearanceOfferRequest;
import com.smartdine.dto.FoodItemDto;
import com.smartdine.exception.EstablishmentNotFoundException;
import com.smartdine.exception.FoodNotAvailableException;
import com.smartdine.model.ClearanceOffer;
import com.smartdine.model.Establishment;
import com.smartdine.model.FoodCategory;
import com.smartdine.model.FoodItem;
import com.smartdine.repository.ClearanceOfferRepository;
import com.smartdine.repository.EstablishmentRepository;
import com.smartdine.repository.FoodCategoryRepository;
import com.smartdine.repository.FoodItemRepository;
import com.smartdine.service.FoodService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class FoodServiceImpl implements FoodService {

    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final EstablishmentRepository establishmentRepository;
    private final ClearanceOfferRepository clearanceOfferRepository;

    public FoodServiceImpl(FoodItemRepository foodItemRepository,
                           FoodCategoryRepository foodCategoryRepository,
                           EstablishmentRepository establishmentRepository,
                           ClearanceOfferRepository clearanceOfferRepository) {
        this.foodItemRepository = foodItemRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.establishmentRepository = establishmentRepository;
        this.clearanceOfferRepository = clearanceOfferRepository;
    }

    @Override
    public List<FoodItem> getMenuByEstablishment(Long establishmentId) {
        return foodItemRepository.findByEstablishment_EstablishmentId(establishmentId);
    }

    @Override
    public FoodItem getFoodItemById(Long foodId) {
        return foodItemRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotAvailableException(foodId, "Item #" + foodId));
    }

    @Override
    public FoodItem addFoodItem(FoodItemDto dto) {
        Establishment establishment = establishmentRepository.findById(dto.getEstablishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(dto.getEstablishmentId()));

        FoodItem item = new FoodItem();
        item.setEstablishment(establishment);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setImageUrl(dto.getImageUrl());
        item.setPrice(dto.getPrice());
        item.setOfferPrice(dto.getOfferPrice());
        item.setClearancePrice(dto.getClearancePrice());
        item.setIsVeg(dto.getIsVeg() != null ? dto.getIsVeg() : true);
        item.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        item.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 50);

        if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()) {
            FoodCategory cat = foodCategoryRepository.findByNameIgnoreCase(dto.getCategoryName())
                    .orElseGet(() -> foodCategoryRepository.save(new FoodCategory(dto.getCategoryName(), 0)));
            item.setCategory(cat);
        }

        if (dto.getTags() != null) {
            item.setTags(new HashSet<>(dto.getTags()));
        }

        return foodItemRepository.save(item);
    }

    @Override
    public FoodItem updateFoodItem(Long foodId, FoodItemDto dto) {
        FoodItem item = getFoodItemById(foodId);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());
        item.setPrice(dto.getPrice());
        item.setOfferPrice(dto.getOfferPrice());
        item.setClearancePrice(dto.getClearancePrice());
        if (dto.getIsVeg() != null) item.setIsVeg(dto.getIsVeg());
        if (dto.getIsAvailable() != null) item.setIsAvailable(dto.getIsAvailable());
        if (dto.getStockQuantity() != null) item.setStockQuantity(dto.getStockQuantity());

        if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()) {
            FoodCategory cat = foodCategoryRepository.findByNameIgnoreCase(dto.getCategoryName())
                    .orElseGet(() -> foodCategoryRepository.save(new FoodCategory(dto.getCategoryName(), 0)));
            item.setCategory(cat);
        }

        if (dto.getTags() != null) {
            item.setTags(new HashSet<>(dto.getTags()));
        }

        return foodItemRepository.save(item);
    }

    @Override
    public void deleteFoodItem(Long foodId) {
        foodItemRepository.deleteById(foodId);
    }

    @Override
    public FoodItem toggleAvailability(Long foodId) {
        FoodItem item = getFoodItemById(foodId);
        item.setIsAvailable(!item.getIsAvailable());
        return foodItemRepository.save(item);
    }

    @Override
    public ClearanceOffer createClearanceOffer(ClearanceOfferRequest request) {
        Establishment establishment = establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(request.getEstablishmentId()));

        FoodItem item = getFoodItemById(request.getFoodId());

        ClearanceOffer offer = new ClearanceOffer(
                establishment,
                item,
                request.getDiscountPercentage(),
                request.getQuantity(),
                request.getStartTime(),
                request.getEndTime()
        );

        // Update food item clearance price dynamically
        item.setClearancePrice(offer.getClearancePrice());
        foodItemRepository.save(item);

        return clearanceOfferRepository.save(offer);
    }

    @Override
    public List<ClearanceOffer> getActiveClearanceOffers(Long establishmentId) {
        return clearanceOfferRepository.findByEstablishment_EstablishmentIdAndIsActiveTrue(establishmentId);
    }

    @Override
    public List<ClearanceOffer> getAllActiveClearanceOffers() {
        return clearanceOfferRepository.findAllActiveClearances();
    }

    @Override
    public void expireClearanceOffer(Long offerId) {
        clearanceOfferRepository.findById(offerId).ifPresent(offer -> {
            offer.setIsActive(false);
            if (offer.getFoodItem() != null) {
                offer.getFoodItem().setClearancePrice(null);
                foodItemRepository.save(offer.getFoodItem());
            }
            clearanceOfferRepository.save(offer);
        });
    }

    @Override
    public List<FoodCategory> getAllCategories() {
        return foodCategoryRepository.findAll();
    }
}
