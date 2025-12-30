package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.dto.request.LoginRequest;
import com.amante.clinicmanagement.dto.request.RegisterRequest;
import com.amante.clinicmanagement.dto.response.AuthResponse;
import com.amante.clinicmanagement.entity.Doctor;
import com.amante.clinicmanagement.entity.Patient;
import com.amante.clinicmanagement.entity.User;
import com.amante.clinicmanagement.repository.DoctorRepository;
import com.amante.clinicmanagement.repository.PatientRepository;
import com.amante.clinicmanagement.repository.UserRepository;
import com.amante.clinicmanagement.security.JwtTokenProvider;
import com.amante.clinicmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}",
                    request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Long profileId = null;
        if (request.getRole() == User.Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setFirstName(request.getFirstName());
            doctor.setLastName(request.getLastName());
            doctor.setSpecialization(request.getSpecialization());
            doctor.setBio(request.getBio());
            doctor.setConsultationFee(request.getConsultationFee());

            String currency = request.getCurrency();
            if (currency != null) {
                doctor.setCurrency(currency);
            } else {
                doctor.setCurrency("PHP");
            }

            doctor.setClinicCountry(request.getClinicCountry());
            doctor.setClinicCity(request.getClinicCity());
            doctor.setClinicAddress(request.getClinicAddress());
            doctor = doctorRepository.save(doctor);
            profileId = doctor.getId();
        } else if (request.getRole() == User.Role.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setFirstName(request.getFirstName());
            patient.setLastName(request.getLastName());
            patient.setPhone(request.getPhone());
            patient.setBirthDate(request.getBirthDate());
            patient.setGender(request.getGender());
            patient = patientRepository.save(patient);
            profileId = patient.getId();
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        log.info("User registered successfully: {}", user.getEmail());

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole(),
                profileId,
                request.getFirstName(),
                request.getLastName()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        if (user.getRole() == User.Role.DOCTOR) {
            doctorRepository.findByUserId(user.getId()).ifPresent(
                    (Doctor doctor) -> {
                        response.setProfileId(doctor.getId());
                        response.setFirstName(doctor.getFirstName());
                        response.setLastName(doctor.getLastName());
                    });
        } else if (user.getRole() == User.Role.PATIENT) {
            patientRepository.findByUserId(user.getId()).ifPresent(
                    (Patient patient) -> {
                        response.setProfileId(patient.getId());
                        response.setFirstName(patient.getFirstName());
                        response.setLastName(patient.getLastName());
                    });
        }

        log.info("Login successful for: {}", request.getEmail());
        return response;
    }
}