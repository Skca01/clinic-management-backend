package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.UpdateDoctorProfileRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.dto.response.DoctorDto;
import com.amante.clinicmanagement.dto.response.TimeSlotDto;
import com.amante.clinicmanagement.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DoctorController doctorController;

    private DoctorDto sampleDoctorDto;
    private final String TEST_EMAIL = "doctor@test.com";

    @BeforeEach
    void setUp() {
        // Setup reusable DoctorDto
        sampleDoctorDto = new DoctorDto(
                1L,
                "Sarah",
                "Smith",
                "Cardiology",
                "Expert bio",
                new BigDecimal("100.00"),
                "USD",
                TEST_EMAIL,
                "USA",
                "NY",
                "123 Clinic Way",
                "http://image-url.com/pic.jpg"
        );
    }

    @Test
    void testGetAllDoctors() {
        // Arrange
        String specialization = "Cardiology";
        String country = "USA";
        String city = "NY";
        List<DoctorDto> list = Collections.singletonList(sampleDoctorDto);

        when(doctorService.searchDoctors(country, city, specialization)).thenReturn(list);

        // Act
        ResponseEntity<ApiResponse<List<DoctorDto>>> response =
                doctorController.getAllDoctors(specialization, country, city);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Doctors retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());

        verify(doctorService).searchDoctors(country, city, specialization);
    }

    @Test
    void testGetDoctorById() {
        // Arrange
        Long doctorId = 1L;
        when(doctorService.getDoctorById(doctorId)).thenReturn(sampleDoctorDto);

        // Act
        ResponseEntity<ApiResponse<DoctorDto>> response = doctorController.getDoctorById(doctorId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Doctor retrieved successfully", response.getBody().getMessage());
        assertEquals(sampleDoctorDto, response.getBody().getData());

        verify(doctorService).getDoctorById(doctorId);
    }

    @Test
    void testGetAvailability() {
        // Arrange
        Long doctorId = 1L;
        LocalDate date = LocalDate.now();

        // Fixed: Create TimeSlotDto using no-args constructor and setters
        TimeSlotDto slot = new TimeSlotDto();
        slot.setStartTime(LocalDateTime.now());
        slot.setEndTime(LocalDateTime.now().plusHours(1));
        slot.setAvailable(true);
        slot.setReason(null);

        List<TimeSlotDto> slots = Collections.singletonList(slot);

        when(doctorService.getAvailableSlots(doctorId, date)).thenReturn(slots);

        // Act
        ResponseEntity<ApiResponse<List<TimeSlotDto>>> response =
                doctorController.getAvailability(doctorId, date);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Available slots retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());

        verify(doctorService).getAvailableSlots(doctorId, date);
    }

    @Test
    void testUpdateDoctorProfile() {
        // Arrange
        UpdateDoctorProfileRequest request = new UpdateDoctorProfileRequest(
                "Sarah", "Smith", "Cardiology", "New Bio",
                new BigDecimal("150.00"), "USD", "USA", "NY", "New Address"
        );

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.updateDoctorProfile(eq(TEST_EMAIL), any(UpdateDoctorProfileRequest.class)))
                .thenReturn(sampleDoctorDto);

        // Act
        ResponseEntity<ApiResponse<DoctorDto>> response =
                doctorController.updateDoctorProfile(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile updated successfully", response.getBody().getMessage());
        assertEquals(sampleDoctorDto, response.getBody().getData());

        verify(doctorService).updateDoctorProfile(TEST_EMAIL, request);
    }

    @Test
    void testUploadProfilePicture() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.uploadProfilePicture(eq(TEST_EMAIL), any(MultipartFile.class)))
                .thenReturn(sampleDoctorDto);

        // Act
        ResponseEntity<ApiResponse<DoctorDto>> response =
                doctorController.uploadProfilePicture(file, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile picture uploaded successfully", response.getBody().getMessage());
        assertEquals(sampleDoctorDto, response.getBody().getData());

        verify(doctorService).uploadProfilePicture(TEST_EMAIL, file);
    }

    @Test
    void testDeleteProfilePicture() throws IOException {
        // Arrange
        when(authentication.getName()).thenReturn(TEST_EMAIL);
        // deleteProfilePicture returns void, so we just verify the call

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                doctorController.deleteProfilePicture(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile picture deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(doctorService).deleteProfilePicture(TEST_EMAIL);
    }
}