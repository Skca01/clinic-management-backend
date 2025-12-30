package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.DiagnosisRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.entity.MedicalRecord;
import com.amante.clinicmanagement.service.MedicalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordControllerTest {

    @Mock
    private MedicalRecordService medicalRecordService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MedicalRecordController medicalRecordController;

    private MedicalRecord sampleRecord;
    private final String TEST_EMAIL = "doctor@test.com";

    @BeforeEach
    void setUp() {
        // Setup reusable MedicalRecord entity
        // We pass null for Appointment to avoid mocking extra dependencies,
        // as the controller doesn't inspect the Appointment object itself.
        sampleRecord = new MedicalRecord(
                1L,
                null, // Appointment entity
                "Common Cold",
                "Rest and plenty of fluids",
                LocalDateTime.now()
        );
    }

    @Test
    void testAddDiagnosis() {
        // Arrange
        DiagnosisRequest request = new DiagnosisRequest(100L, "Common Cold", "Rest");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(medicalRecordService.addDiagnosis(any(DiagnosisRequest.class), eq(TEST_EMAIL)))
                .thenReturn(sampleRecord);

        // Act
        ResponseEntity<ApiResponse<MedicalRecord>> response =
                medicalRecordController.addDiagnosis(request, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Diagnosis added successfully", response.getBody().getMessage());
        assertEquals(sampleRecord, response.getBody().getData());

        verify(medicalRecordService).addDiagnosis(request, TEST_EMAIL);
    }

    @Test
    void testGetMedicalRecord() {
        // Arrange
        Long appointmentId = 100L;

        when(medicalRecordService.getMedicalRecordByAppointmentId(appointmentId))
                .thenReturn(sampleRecord);

        // Act
        ResponseEntity<ApiResponse<MedicalRecord>> response =
                medicalRecordController.getMedicalRecord(appointmentId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Medical record retrieved successfully", response.getBody().getMessage());
        assertEquals(sampleRecord, response.getBody().getData());

        verify(medicalRecordService).getMedicalRecordByAppointmentId(appointmentId);
    }
}