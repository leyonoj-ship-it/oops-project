package com.smartdine.controller;

import com.smartdine.dto.DashboardStatsDto;
import com.smartdine.model.Establishment;
import com.smartdine.model.Person;
import com.smartdine.model.UserStatus;
import com.smartdine.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboard(@RequestParam(defaultValue = "all") String timeRange) {
        return ResponseEntity.ok(adminService.getPlatformAnalytics(timeRange));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Person>> getAllUsers(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getAllUsers(search));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Person> updateUserStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, status));
    }

    @GetMapping("/establishments")
    public ResponseEntity<List<Establishment>> getAllEstablishments() {
        return ResponseEntity.ok(adminService.getAllEstablishments());
    }

    @PatchMapping("/establishments/{id}/status")
    public ResponseEntity<Establishment> updateEstablishmentStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        return ResponseEntity.ok(adminService.updateEstablishmentStatus(id, status));
    }
}
