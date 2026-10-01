package com.smartdine.service;

import com.smartdine.dto.AuthResponse;
import com.smartdine.dto.LoginRequest;
import com.smartdine.model.Person;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse verifyGoogleToken(String idToken, LoginRequest request);
    Person getAuthenticatedUser(String token);
    AuthResponse quickDemoLogin(String roleName, Long establishmentId);
}
