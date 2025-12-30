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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest doctorRegisterRequest;
    private RegisterRequest patientRegisterRequest;
    private LoginRequest loginRequest;
    private User user;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Setup Doctor Register Request
        doctorRegisterRequest = new RegisterRequest();
        doctorRegisterRequest.setEmail("doctor@test.com");
        doctorRegisterRequest.setPassword("password123");
        doctorRegisterRequest.setRole(User.Role.DOCTOR);
        doctorRegisterRequest.setFirstName("Kent");
        doctorRegisterRequest.setLastName("Carlo");
        doctorRegisterRequest.setSpecialization("Cardiology");
        doctorRegisterRequest.setBio("Experienced cardiologist");
        doctorRegisterRequest.setConsultationFee(new BigDecimal("1500.00"));
        doctorRegisterRequest.setCurrency("USD");
        doctorRegisterRequest.setClinicCountry("USA");
        doctorRegisterRequest.setClinicCity("New York");
        doctorRegisterRequest.setClinicAddress("123 Main St");

        // Setup Patient Register Request
        patientRegisterRequest = new RegisterRequest();
        patientRegisterRequest.setEmail("patient@test.com");
        patientRegisterRequest.setPassword("password123");
        patientRegisterRequest.setRole(User.Role.PATIENT);
        patientRegisterRequest.setFirstName("Jane");
        patientRegisterRequest.setLastName("Smith");
        patientRegisterRequest.setPhone("1234567890");
        patientRegisterRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        patientRegisterRequest.setGender("Female");

        // Setup Login Request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");

        // Setup User
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
        user.setRole(User.Role.DOCTOR);
        user.setIsActive(true);

        // Setup Doctor
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(user);
        doctor.setFirstName("Kent");
        doctor.setLastName("Carlo");
        doctor.setSpecialization("Cardiology");

        // Setup Patient
        patient = new Patient();
        patient.setId(1L);
        patient.setUser(user);
        patient.setFirstName("Jane");
        patient.setLastName("Smith");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void register_DoctorScenarios_SuccessAndCurrencyHandling() {
        // Test 1: Doctor with currency
        when(userRepository.existsByEmail(doctorRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(doctorRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor savedDoctor = invocation.getArgument(0);
            savedDoctor.setId(1L);
            return savedDoctor;
        });
        when(jwtTokenProvider.generateToken(doctorRegisterRequest.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.register(doctorRegisterRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1L, response.getUserId());
        assertEquals("doctor@test.com", response.getEmail());
        assertEquals(User.Role.DOCTOR, response.getRole());
        assertEquals(1L, response.getProfileId());
        assertEquals("Kent", response.getFirstName());
        assertEquals("Carlo", response.getLastName());
        verify(doctorRepository, times(1)).save(any(Doctor.class));

        // Test 2: Doctor without currency defaults to PHP
        doctorRegisterRequest.setCurrency(null);
        doctorRegisterRequest.setEmail("doctor2@test.com");
        when(userRepository.existsByEmail("doctor2@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor savedDoctor = invocation.getArgument(0);
            savedDoctor.setId(2L);
            // Verify PHP default is set
            assertEquals("PHP", savedDoctor.getCurrency());
            return savedDoctor;
        });
        when(jwtTokenProvider.generateToken("doctor2@test.com")).thenReturn("jwt-token-2");

        response = authService.register(doctorRegisterRequest);

        assertNotNull(response);
        assertEquals("jwt-token-2", response.getToken());
        // Verify total of 2 doctor saves happened
        verify(doctorRepository, times(2)).save(any(Doctor.class));
    }

    @Test
    void register_PatientAndOtherRoles_Success() {
        // Test 1: Patient registration
        when(userRepository.existsByEmail(patientRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(patientRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient savedPatient = invocation.getArgument(0);
            savedPatient.setId(2L);
            return savedPatient;
        });
        when(jwtTokenProvider.generateToken(patientRegisterRequest.getEmail())).thenReturn("jwt-token-patient");

        AuthResponse response = authService.register(patientRegisterRequest);

        assertNotNull(response);
        assertEquals("jwt-token-patient", response.getToken());
        assertEquals(2L, response.getUserId());
        assertEquals("patient@test.com", response.getEmail());
        assertEquals(User.Role.PATIENT, response.getRole());
        assertEquals(2L, response.getProfileId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        verify(patientRepository, times(1)).save(any(Patient.class));

        // Test 2: Role neither doctor nor patient (no profile created)
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setEmail("admin@test.com");
        adminRequest.setPassword("password123");
        adminRequest.setRole(null);
        adminRequest.setFirstName("Admin");
        adminRequest.setLastName("User");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L);
            return savedUser;
        });
        when(jwtTokenProvider.generateToken("admin@test.com")).thenReturn("jwt-token-admin");

        response = authService.register(adminRequest);

        assertNotNull(response);
        assertNull(response.getProfileId());

        // Verify final counts: patient saved once, no doctor saves in this test
        verify(patientRepository, times(1)).save(any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(doctorRegisterRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(doctorRegisterRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(doctorRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void login_DoctorAndPatient_SuccessScenarios() {
        // Test 1: Doctor user success
        user.setRole(User.Role.DOCTOR);
        loginRequest.setEmail("doctor@test.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findByUserId(user.getId())).thenReturn(Optional.of(doctor));
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(User.Role.DOCTOR, response.getRole());
        assertEquals(1L, response.getProfileId());
        assertEquals("Kent", response.getFirstName());

        // Test 2: Patient user success
        user.setRole(User.Role.PATIENT);
        loginRequest.setEmail("patient@test.com");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(user.getId())).thenReturn(Optional.of(patient));
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt-token-patient");

        response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token-patient", response.getToken());
        assertEquals(User.Role.PATIENT, response.getRole());
        assertEquals(1L, response.getProfileId());
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void login_ProfileNotFound_ReturnsNullProfileId() {
        // Test 1: Doctor not found
        user.setRole(User.Role.DOCTOR);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNull(response.getProfileId());
        assertNull(response.getFirstName());
        verify(doctorRepository).findByUserId(user.getId());

        // Test 2: Patient not found
        user.setRole(User.Role.PATIENT);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        response = authService.login(loginRequest);

        assertNotNull(response);
        assertNull(response.getProfileId());
        verify(patientRepository).findByUserId(user.getId());

        // Test 3: Role neither doctor nor patient
        user.setRole(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        response = authService.login(loginRequest);

        assertNotNull(response);
        assertNull(response.getProfileId());
        // Verify repositories not called again for null role
        verify(doctorRepository, times(1)).findByUserId(anyLong());
        verify(patientRepository, times(1)).findByUserId(anyLong());
    }

    @Test
    void login_ErrorCases_ThrowsExceptions() {
        // Test 1: User not found
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("User not found", exception.getMessage());

        // Test 2: Inactive account
        user.setIsActive(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Account is inactive", exception.getMessage());

        verify(jwtTokenProvider, never()).generateToken(anyString());
    }
}