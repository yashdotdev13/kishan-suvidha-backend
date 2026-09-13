package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.controller;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos.*;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.UserRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.service.AuthService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.service.LocationService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final LocationService locationService;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(new LoginResponse(null, "Bearer", user.getId(), user.getEmail(), user.getRole()));
    }


    @PutMapping("/farmer/me/location")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Void> updateFarmerLocation(
            @Valid @RequestBody UpdateLocationRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        locationService.updateFarmerLocation(
                userId,
                request.location()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/buyer/me/location")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Void> updateBuyerLocation(
            @Valid @RequestBody UpdateLocationRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        locationService.updateBuyerLocation(
                userId,
                request.location()
        );

        return ResponseEntity.noContent().build();
    }
}
