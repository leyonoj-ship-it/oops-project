package com.smartdine.controller;

import com.smartdine.dto.AuthResponse;
import com.smartdine.dto.LoginRequest;
import com.smartdine.model.Person;
import com.smartdine.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google-verify")
    public ResponseEntity<AuthResponse> verifyGoogle(@RequestBody LoginRequest request) {
        AuthResponse response = authService.verifyGoogleToken(request.getGoogleToken(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quick-demo")
    public ResponseEntity<AuthResponse> quickDemo(@RequestBody Map<String, Object> payload) {
        String role = (String) payload.getOrDefault("role", "CUSTOMER");
        Long establishmentId = payload.get("establishmentId") != null
                ? Long.valueOf(payload.get("establishmentId").toString())
                : null;
        AuthResponse response = authService.quickDemoLogin(role, establishmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Person> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Person person = authService.getAuthenticatedUser(token);
        return ResponseEntity.ok(person);
    }
}
