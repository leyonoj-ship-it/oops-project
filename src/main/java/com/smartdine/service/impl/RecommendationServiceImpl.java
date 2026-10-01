package com.smartdine.service.impl;

import com.smartdine.model.AmbianceType;
import com.smartdine.model.Establishment;
import com.smartdine.model.FoodItem;
import com.smartdine.model.UserStatus;
import com.smartdine.repository.EstablishmentRepository;
import com.smartdine.repository.FoodItemRepository;
import com.smartdine.service.RecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final FoodItemRepository foodItemRepository;
    private final EstablishmentRepository establishmentRepository;

    public RecommendationServiceImpl(FoodItemRepository foodItemRepository,
                                     EstablishmentRepository establishmentRepository) {
        this.foodItemRepository = foodItemRepository;
        this.establishmentRepository = establishmentRepository;
    }

    @Override
    public List<FoodItem> getRecommendedFoodForAmbiance(AmbianceType ambiance, Long establishmentId) {
        if (ambiance == null || ambiance == AmbianceType.DEFAULT) {
            if (establishmentId != null) {
                return foodItemRepository.findByEstablishment_EstablishmentIdAndIsAvailableTrue(establishmentId);
            }
            return foodItemRepository.findAll().stream()
                    .filter(f -> Boolean.TRUE.equals(f.getIsAvailable()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        String tag = ambiance.name();
        List<FoodItem> items;
        if (establishmentId != null) {
            items = foodItemRepository.findByEstablishmentAndAmbianceTag(establishmentId, tag);
            // If few specific items found, fallback to establishment's bestsellers
            if (items.isEmpty()) {
                items = foodItemRepository.findByEstablishment_EstablishmentIdAndIsAvailableTrue(establishmentId);
            }
        } else {
            items = foodItemRepository.findByAmbianceTag(tag);
        }

        return items;
    }

    @Override
    public List<Establishment> getRecommendedEstablishmentsForAmbiance(AmbianceType ambiance) {
        if (ambiance == null || ambiance == AmbianceType.DEFAULT) {
            return establishmentRepository.findByStatus(UserStatus.ACTIVE);
        }
        return establishmentRepository.findByStatus(UserStatus.ACTIVE).stream()
                .filter(e -> e.getSupportedAmbiances().contains(ambiance))
                .collect(Collectors.toList());
    }
}
