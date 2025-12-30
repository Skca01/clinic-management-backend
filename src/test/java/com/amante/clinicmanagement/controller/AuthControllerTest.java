package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.LoginRequest;
import com.amante.clinicmanagement.dto.request.RegisterRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.dto.response.AuthResponse;
import com.amante.clinicmanagement.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse sampleAuthResponse;

    @BeforeEach
    void setUp() {
        // Setup a reusable response object
        sampleAuthResponse = new AuthResponse(
                "dummy-jwt-token",
                "Bearer",
                1L,
                "test@example.com",
                null, // Passing null for User.Role to avoid dependency on unknown Enum values
                100L,
                "Kent",
                "Carlo"
        );
    }

    @Test
    void testRegister() {
        // Arrange
        // Constructing the full request. Passing null for Role to ensure compilation.
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "password123",
                null, // User.Role
                "Kent",
                "Carlp",
                "1234567890",
                LocalDate.of(1990, 1, 1),
                "Male",
                "Cardiology",
                "Bio...",
                BigDecimal.TEN,
                "USD",
                "USA",
                "NY",
                "123 Street"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse);

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Registration successful", response.getBody().getMessage());
        assertEquals(sampleAuthResponse, response.getBody().getData());
        assertEquals("dummy-jwt-token", response.getBody().getData().getToken());

        verify(authService).register(request);
    }

    @Test
    void testLogin() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse);

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(sampleAuthResponse, response.getBody().getData());

        verify(authService).login(request);
    }
}