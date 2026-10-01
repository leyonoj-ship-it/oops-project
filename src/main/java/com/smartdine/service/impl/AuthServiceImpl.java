package com.smartdine.service.impl;

import com.smartdine.dto.AuthResponse;
import com.smartdine.dto.LoginRequest;
import com.smartdine.exception.UnauthorizedAccessException;
import com.smartdine.exception.UserNotFoundException;
import com.smartdine.model.*;
import com.smartdine.repository.*;
import com.smartdine.service.AuthService;
import com.smartdine.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final PersonRepository personRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final EstablishmentOwnerRepository establishmentOwnerRepository;
    private final EstablishmentRepository establishmentRepository;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(PersonRepository personRepository,
                           CustomerRepository customerRepository,
                           AdminRepository adminRepository,
                           EstablishmentOwnerRepository establishmentOwnerRepository,
                           EstablishmentRepository establishmentRepository,
                           JwtUtil jwtUtil) {
        this.personRepository = personRepository;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.establishmentOwnerRepository = establishmentOwnerRepository;
        this.establishmentRepository = establishmentRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Enforce backend role verification: never blindly trust the role
        Optional<Person> personOpt = personRepository.findByEmail(request.getEmail().toLowerCase().trim());
        Person person;

        if (personOpt.isPresent()) {
            person = personOpt.get();
            // Verify account status
            if (person.getStatus() == UserStatus.BLOCKED) {
                throw new UnauthorizedAccessException("Your account has been blocked by Platform Administration.");
            }
            if (person.getStatus() == UserStatus.SUSPENDED) {
                throw new UnauthorizedAccessException("Your account is temporarily suspended.");
            }
            // Verify matching role
            if (person.getRole() != request.getRole()) {
                throw new UnauthorizedAccessException("Account exists but does not have '" + request.getRole() + "' privileges. Selected role mismatch.");
            }
        } else {
            // Auto-provision Customer on first login if logging in as Customer
            if (request.getRole() == UserRole.CUSTOMER) {
                String displayName = request.getName() != null ? request.getName() : request.getEmail().split("@")[0];
                Customer newCust = new Customer(displayName, request.getEmail().toLowerCase().trim(), "9876543210");
                person = customerRepository.save(newCust);
            } else {
                throw new UserNotFoundException("No active " + request.getRole() + " account found with email: " + request.getEmail());
            }
        }

        return createAuthResponse(person, request.getEstablishmentId());
    }

    @Override
    public AuthResponse verifyGoogleToken(String idToken, LoginRequest request) {
        // In academic / local mode, simulate Google ID token decoding or auto-provision
        String email = request.getEmail().toLowerCase().trim();
        Optional<Person> personOpt = personRepository.findByEmail(email);
        Person person;

        if (personOpt.isPresent()) {
            person = personOpt.get();
            if (person.getRole() != request.getRole()) {
                throw new UnauthorizedAccessException("Google authenticated email does not have role: " + request.getRole());
            }
        } else {
            if (request.getRole() == UserRole.CUSTOMER) {
                String name = request.getName() != null ? request.getName() : email.split("@")[0];
                Customer customer = new Customer(name, email, "9876543210");
                customer.setGoogleId(idToken != null ? idToken.substring(0, Math.min(20, idToken.length())) : "google-" + System.currentTimeMillis());
                person = customerRepository.save(customer);
            } else {
                throw new UnauthorizedAccessException("Only customers can auto-register via Google. Hotel and Admin accounts must be pre-authorized.");
            }
        }

        return createAuthResponse(person, request.getEstablishmentId());
    }

    @Override
    public Person getAuthenticatedUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtil.isTokenValid(token)) {
            throw new UnauthorizedAccessException("Invalid or expired session token.");
        }
        Long userId = jwtUtil.extractUserId(token);
        return personRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));
    }

    @Override
    public AuthResponse quickDemoLogin(String roleName, Long establishmentId) {
        UserRole role = UserRole.valueOf(roleName.toUpperCase());
        Person person;

        switch (role) {
            case CUSTOMER -> {
                person = customerRepository.findByEmail("customer@smartdine.com")
                        .orElseGet(() -> customerRepository.save(new Customer("Aarav Sharma", "customer@smartdine.com", "9845012345")));
            }
            case ESTABLISHMENT_OWNER -> {
                if (establishmentId != null) {
                    Establishment est = establishmentRepository.findById(establishmentId)
                            .orElseThrow(() -> new UserNotFoundException("Establishment not found"));
                    if (est.getOwner() != null) {
                        person = est.getOwner();
                    } else {
                        EstablishmentOwner owner = establishmentOwnerRepository.save(new EstablishmentOwner("Rajesh Verma", "owner" + establishmentId + "@smartdine.com", "9812345678"));
                        est.setOwner(owner);
                        establishmentRepository.save(est);
                        person = owner;
                    }
                } else {
                    person = establishmentOwnerRepository.findByEmail("hotel@smartdine.com")
                            .orElseGet(() -> establishmentOwnerRepository.save(new EstablishmentOwner("Chef Vikram Malhotra", "hotel@smartdine.com", "9811223344")));
                }
            }
            case ADMIN -> {
                person = adminRepository.findByEmail("admin@smartdine.com")
                        .orElseGet(() -> adminRepository.save(new Admin("Dr. Priya Rao", "admin@smartdine.com", "9800112233", "Platform Security & Operations", 5)));
            }
            default -> throw new IllegalArgumentException("Unknown role: " + roleName);
        }

        return createAuthResponse(person, establishmentId);
    }

    private AuthResponse createAuthResponse(Person person, Long requestedEstId) {
        String token = jwtUtil.generateToken(person.getId(), person.getEmail(), person.getRole());
        AuthResponse response = new AuthResponse(token, person.getId(), person.getName(), person.getEmail(), person.getRole(), person.getStatus());

        if (person.getRole() == UserRole.ESTABLISHMENT_OWNER) {
            Optional<Establishment> estOpt = Optional.empty();
            if (requestedEstId != null) {
                estOpt = establishmentRepository.findById(requestedEstId);
            }
            if (estOpt.isEmpty()) {
                estOpt = establishmentRepository.findByOwnerId(person.getId());
            }
            if (estOpt.isEmpty() && !establishmentRepository.findAll().isEmpty()) {
                estOpt = Optional.of(establishmentRepository.findAll().get(0));
            }
            estOpt.ifPresent(est -> {
                response.setEstablishmentId(est.getEstablishmentId());
                response.setEstablishmentName(est.getName());
                response.setEstablishmentType(est.getType().name());
            });
        }
        response.setMessage("Authenticated successfully as " + person.getRole());
        return response;
    }
}
