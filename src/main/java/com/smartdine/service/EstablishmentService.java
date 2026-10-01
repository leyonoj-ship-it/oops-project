package com.smartdine.service;

import com.smartdine.dto.DashboardStatsDto;
import com.smartdine.model.AmbianceType;
import com.smartdine.model.Establishment;
import com.smartdine.model.EstablishmentType;

import java.util.List;

public interface EstablishmentService {
    List<Establishment> getAllEstablishments();
    List<Establishment> getNearbyEstablishments(double userLat, double userLon, Double maxRadiusKm, EstablishmentType type);
    Establishment getEstablishmentById(Long establishmentId);
    List<Establishment> getEstablishmentsByType(EstablishmentType type);
    List<Establishment> searchEstablishments(String query);
    List<Establishment> getEstablishmentsByAmbiance(AmbianceType ambiance);
    Establishment saveEstablishment(Establishment establishment);
    DashboardStatsDto getEstablishmentDashboardStats(Long establishmentId);
}
