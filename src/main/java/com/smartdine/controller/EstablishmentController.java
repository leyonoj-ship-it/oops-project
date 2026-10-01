package com.smartdine.controller;

import com.smartdine.dto.DashboardStatsDto;
import com.smartdine.model.AmbianceType;
import com.smartdine.model.Establishment;
import com.smartdine.model.EstablishmentType;
import com.smartdine.service.EstablishmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/establishments")
@CrossOrigin(originPatterns = "*")
public class EstablishmentController {

    private final EstablishmentService establishmentService;

    public EstablishmentController(EstablishmentService establishmentService) {
        this.establishmentService = establishmentService;
    }

    @GetMapping
    public ResponseEntity<List<Establishment>> getAllEstablishments() {
        return ResponseEntity.ok(establishmentService.getAllEstablishments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Establishment> getEstablishmentById(@PathVariable Long id) {
        return ResponseEntity.ok(establishmentService.getEstablishmentById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Establishment>> getNearby(
            @RequestParam(defaultValue = "12.9716") double lat,
            @RequestParam(defaultValue = "77.5946") double lon,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) EstablishmentType type) {
        return ResponseEntity.ok(establishmentService.getNearbyEstablishments(lat, lon, radius, type));
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Establishment>> getByType(@PathVariable EstablishmentType type) {
        return ResponseEntity.ok(establishmentService.getEstablishmentsByType(type));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Establishment>> search(@RequestParam String query) {
        return ResponseEntity.ok(establishmentService.searchEstablishments(query));
    }

    @GetMapping("/ambiance/{ambiance}")
    public ResponseEntity<List<Establishment>> getByAmbiance(@PathVariable AmbianceType ambiance) {
        return ResponseEntity.ok(establishmentService.getEstablishmentsByAmbiance(ambiance));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(@PathVariable Long id) {
        return ResponseEntity.ok(establishmentService.getEstablishmentDashboardStats(id));
    }
}
