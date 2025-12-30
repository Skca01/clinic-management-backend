package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.BookAppointmentRequest;
import com.amante.clinicmanagement.dto.request.RejectAppointmentRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.dto.response.AppointmentDto;
import com.amante.clinicmanagement.service.AppointmentService;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentDto sampleAppointmentDto;
    private final String TEST_EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        // Create a reusable sample DTO for assertions
        sampleAppointmentDto = AppointmentDto.builder()
                .id(1L)
                .patientId(100L)
                .patientName("Kent Carlo")
                .doctorId(200L)
                .doctorName("Dr. Smith")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(30))
                .status("PENDING")
                .build();
    }

    @Test
    void testBookAppointment() {
        // Arrange
        BookAppointmentRequest request = new BookAppointmentRequest(200L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), "Headache");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.bookAppointment(any(BookAppointmentRequest.class), eq(TEST_EMAIL)))
                .thenReturn(sampleAppointmentDto);

        // Act
        ResponseEntity<ApiResponse<AppointmentDto>> response = appointmentController.bookAppointment(request, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Appointment request submitted!", response.getBody().getMessage());
        assertEquals(sampleAppointmentDto, response.getBody().getData());

        verify(appointmentService).bookAppointment(request, TEST_EMAIL);
    }

    @Test
    void testConfirmAppointment() {
        // Arrange
        Long appointmentId = 1L;
        sampleAppointmentDto.setStatus("CONFIRMED");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.confirmAppointment(appointmentId, TEST_EMAIL))
                .thenReturn(sampleAppointmentDto);

        // Act
        ResponseEntity<ApiResponse<AppointmentDto>> response = appointmentController.confirmAppointment(appointmentId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointment confirmed!", response.getBody().getMessage());
        assertEquals("CONFIRMED", response.getBody().getData().getStatus());

        verify(appointmentService).confirmAppointment(appointmentId, TEST_EMAIL);
    }

    @Test
    void testRejectAppointment() {
        // Arrange
        Long appointmentId = 1L;
        RejectAppointmentRequest request = new RejectAppointmentRequest("Busy schedule");
        sampleAppointmentDto.setStatus("REJECTED");
        sampleAppointmentDto.setRejectionReason("Busy schedule");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.rejectAppointment(eq(appointmentId), eq(TEST_EMAIL), any(RejectAppointmentRequest.class)))
                .thenReturn(sampleAppointmentDto);

        // Act
        ResponseEntity<ApiResponse<AppointmentDto>> response = appointmentController.rejectAppointment(appointmentId, request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointment rejected.", response.getBody().getMessage());
        assertEquals("REJECTED", response.getBody().getData().getStatus());

        verify(appointmentService).rejectAppointment(appointmentId, TEST_EMAIL, request);
    }

    @Test
    void testCompleteAppointment() {
        // Arrange
        Long appointmentId = 1L;
        sampleAppointmentDto.setStatus("COMPLETED");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.completeAppointment(appointmentId, TEST_EMAIL))
                .thenReturn(sampleAppointmentDto);

        // Act
        ResponseEntity<ApiResponse<AppointmentDto>> response = appointmentController.completeAppointment(appointmentId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointment completed.", response.getBody().getMessage());
        assertEquals("COMPLETED", response.getBody().getData().getStatus());

        verify(appointmentService).completeAppointment(appointmentId, TEST_EMAIL);
    }

    @Test
    void testGetMyAppointments() {
        // Arrange
        List<AppointmentDto> list = Arrays.asList(sampleAppointmentDto, sampleAppointmentDto);

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.getMyAppointments(TEST_EMAIL)).thenReturn(list);

        // Act
        ResponseEntity<ApiResponse<List<AppointmentDto>>> response = appointmentController.getMyAppointments(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointments retrieved.", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());

        verify(appointmentService).getMyAppointments(TEST_EMAIL);
    }

    @Test
    void testGetPendingAppointments() {
        // Arrange
        List<AppointmentDto> list = Arrays.asList(sampleAppointmentDto);

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.getDoctorPendingAppointments(TEST_EMAIL)).thenReturn(list);

        // Act
        ResponseEntity<ApiResponse<List<AppointmentDto>>> response = appointmentController.getPendingAppointments(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pending appointments retrieved.", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());

        verify(appointmentService).getDoctorPendingAppointments(TEST_EMAIL);
    }

    @Test
    void testCancelAppointment() {
        // Arrange
        Long appointmentId = 1L;
        sampleAppointmentDto.setStatus("CANCELLED");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(appointmentService.cancelAppointment(appointmentId, TEST_EMAIL))
                .thenReturn(sampleAppointmentDto);

        // Act
        ResponseEntity<ApiResponse<AppointmentDto>> response = appointmentController.cancelAppointment(appointmentId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointment cancelled.", response.getBody().getMessage());
        assertEquals("CANCELLED", response.getBody().getData().getStatus());

        verify(appointmentService).cancelAppointment(appointmentId, TEST_EMAIL);
    }

    @Test
    void testGetAppointmentById() {
        // Arrange
        Long appointmentId = 1L;
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(sampleAppointmentDto);

        // Act
        // Note: This endpoint does NOT use the Authentication object in the controller signature
        ResponseEntity<ApiResponse<AppointmentDto>> response = appointmentController.getAppointmentById(appointmentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointment retrieved.", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());

        verify(appointmentService).getAppointmentById(appointmentId);
    }
}