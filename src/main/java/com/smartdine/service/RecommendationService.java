package com.smartdine.service;

import com.smartdine.model.AmbianceType;
import com.smartdine.model.Establishment;
import com.smartdine.model.FoodItem;

import java.util.List;

public interface RecommendationService {
    List<FoodItem> getRecommendedFoodForAmbiance(AmbianceType ambiance, Long establishmentId);
    List<Establishment> getRecommendedEstablishmentsForAmbiance(AmbianceType ambiance);
}
