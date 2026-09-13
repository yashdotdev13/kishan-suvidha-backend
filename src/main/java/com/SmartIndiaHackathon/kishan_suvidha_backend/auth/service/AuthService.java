package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos.LoginRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos.LoginResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos.RegisterRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos.RegisterResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Officer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.enums.Role;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.BuyerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.OfficerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.UserRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.security.JwtService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.InvalidCredentialsException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final BuyerRepository buyerRepository;
    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // 2. Check duplicate phone
        if (userRepository.existsByPhone(request.phone())) {
            throw new UserAlreadyExistsException("User with this phone number already exists");
        }

        // 3. Validate role-specific data
        validateRoleSpecificData(request);

        // 4. Create User
        User user = User.builder().email(request.email()).phone(request.phone())
                .password(passwordEncoder.encode(request.password())).role(request.role()).enabled(true).build();
        user = userRepository.save(user);

        // 5. Create role-specific profile
        createProfile(request, user);
        // 6. Return response
        return new RegisterResponse(user.getId(), user.getEmail(), user.getPhone(), user.getRole(), "Registration successful");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. Find user by email
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // 2. Check whether the account is enabled
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("User account is disabled");
        }
        // 3. Verify password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        // 4. Generate JWT
        String token = jwtService.generateToken(user);
        // 5. Return login response
        return new LoginResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public LoginResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new LoginResponse(null, "Bearer", user.getId(), user.getEmail(), user.getRole());
    }

    private void validateRoleSpecificData(RegisterRequest request) {

        if (request.role() == Role.FARMER) {
            if (isBlank(request.state()) || isBlank(request.district()) || isBlank(request.village())) {
                throw new BadRequestException("State, district and village are required for farmer registration");
            }
        } else if (request.role() == Role.BUYER) {
            if (isBlank(request.location())) {
                throw new BadRequestException("Location is required for buyer registration");
            }
        }
    }

    private void createProfile(RegisterRequest request, User user) {
        switch (request.role()) {
            case FARMER -> {
                Farmer farmer = Farmer.builder().user(user).name(request.name()).mobile(request.phone()).state(request.state()).district(request.district()).village(request.village()).build();
                farmerRepository.save(farmer);
            }
            case BUYER -> {
                Buyer buyer = Buyer.builder().user(user).name(request.name()).companyName(request.companyName()).location(request.location()).build();
                buyerRepository.save(buyer);
            }
            case OFFICER -> {
                Officer officer = Officer.builder().user(user).name(request.name()).build();
                officerRepository.save(officer);
            }
        }
    }
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}