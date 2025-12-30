package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.*;
import com.amante.clinicmanagement.dto.response.*;
import com.amante.clinicmanagement.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleControllerTest {

    @Mock
    private DoctorService doctorService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DoctorScheduleController doctorScheduleController;

    private final String TEST_EMAIL = "doctor@test.com";

    @BeforeEach
    void setUp() {
        // No common setup needed beyond mocks injection,
        // but we can ensure authentication is mocked for methods that need it
        // inside the specific tests to keep them clean.
    }

    @Test
    void testGetMySchedule() {
        // Arrange
        DoctorScheduleResponse mockResponse = new DoctorScheduleResponse();
        mockResponse.setSettings(new DoctorSettingsDto(1L, 30, 5, "UTC"));

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.getDoctorSchedule(TEST_EMAIL)).thenReturn(mockResponse);

        // Act
        ResponseEntity<DoctorScheduleResponse> response =
                doctorScheduleController.getMySchedule(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(doctorService).getDoctorSchedule(TEST_EMAIL);
    }

    @Test
    void testGetAvailableSlots() {
        // Arrange
        Long doctorId = 1L;
        LocalDate date = LocalDate.now();
        List<TimeSlotDto> mockSlots = Collections.singletonList(new TimeSlotDto());

        when(doctorService.getAvailableSlots(doctorId, date)).thenReturn(mockSlots);

        // Act
        ResponseEntity<List<TimeSlotDto>> response =
                doctorScheduleController.getAvailableSlots(doctorId, date);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSlots, response.getBody());
        verify(doctorService).getAvailableSlots(doctorId, date);
    }

    @Test
    void testUpdateSettings() {
        // Arrange
        UpdateDoctorSettingsRequest request = new UpdateDoctorSettingsRequest(30, 5, "UTC");
        DoctorSettingsDto mockDto = new DoctorSettingsDto(1L, 30, 5, "UTC");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.updateDoctorSettings(eq(TEST_EMAIL), any(UpdateDoctorSettingsRequest.class)))
                .thenReturn(mockDto);

        // Act
        ResponseEntity<DoctorSettingsDto> response =
                doctorScheduleController.updateSettings(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
        verify(doctorService).updateDoctorSettings(TEST_EMAIL, request);
    }

    @Test
    void testUpdateWeeklySchedule() {
        // Arrange
        Map<DayOfWeek, UpdateWeeklyScheduleRequest.DaySchedule> scheduleMap = new HashMap<>();
        scheduleMap.put(DayOfWeek.MONDAY, new UpdateWeeklyScheduleRequest.DaySchedule(true, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        UpdateWeeklyScheduleRequest request = new UpdateWeeklyScheduleRequest(scheduleMap);

        List<WeeklyScheduleDto> mockResponse = Collections.singletonList(new WeeklyScheduleDto());

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.updateWeeklySchedule(eq(TEST_EMAIL), any(UpdateWeeklyScheduleRequest.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<List<WeeklyScheduleDto>> response =
                doctorScheduleController.updateWeeklySchedule(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(doctorService).updateWeeklySchedule(TEST_EMAIL, request);
    }

    @Test
    void testAddBreak() {
        // Arrange
        AddBreakRequest request = new AddBreakRequest("Lunch", LocalTime.of(12, 0), LocalTime.of(13, 0), "MONDAY");
        DoctorBreakDto mockDto = new DoctorBreakDto(1L, "MONDAY", "Lunch", LocalTime.of(12, 0), LocalTime.of(13, 0));

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.addBreak(eq(TEST_EMAIL), any(AddBreakRequest.class)))
                .thenReturn(mockDto);

        // Act
        ResponseEntity<DoctorBreakDto> response =
                doctorScheduleController.addBreak(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
        verify(doctorService).addBreak(TEST_EMAIL, request);
    }

    @Test
    void testUpdateBreak() {
        // Arrange
        Long breakId = 1L;
        AddBreakRequest request = new AddBreakRequest("Lunch Update", LocalTime.of(12, 30), LocalTime.of(13, 30), "MONDAY");
        DoctorBreakDto mockDto = new DoctorBreakDto(breakId, "MONDAY", "Lunch Update", LocalTime.of(12, 30), LocalTime.of(13, 30));

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.updateBreak(eq(TEST_EMAIL), eq(breakId), any(AddBreakRequest.class)))
                .thenReturn(mockDto);

        // Act
        ResponseEntity<DoctorBreakDto> response =
                doctorScheduleController.updateBreak(breakId, request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
        verify(doctorService).updateBreak(TEST_EMAIL, breakId, request);
    }

    @Test
    void testDeleteBreak() {
        // Arrange
        Long breakId = 1L;
        when(authentication.getName()).thenReturn(TEST_EMAIL);

        // Act
        ResponseEntity<Void> response =
                doctorScheduleController.deleteBreak(breakId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(doctorService).deleteBreak(TEST_EMAIL, breakId);
    }

    @Test
    void testAddDayOff() {
        // Arrange
        // FIXED: Using all 6 parameters (startDate, endDate, reason, type, isRecurring, recurringDay)
        AddDayOffRequest request = new AddDayOffRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "Vacation",
                "VACATION",
                false,  // isRecurring
                null    // recurringDay
        );
        DoctorDayOffDto mockDto = new DoctorDayOffDto(1L, LocalDate.now(), LocalDate.now().plusDays(1), "Vacation", "VACATION", false, null);

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.addDayOff(eq(TEST_EMAIL), any(AddDayOffRequest.class)))
                .thenReturn(mockDto);

        // Act
        ResponseEntity<DoctorDayOffDto> response =
                doctorScheduleController.addDayOff(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
        verify(doctorService).addDayOff(TEST_EMAIL, request);
    }

    @Test
    void testUpdateDayOff() {
        // Arrange
        Long dayOffId = 1L;
        // FIXED: Using all 6 parameters (startDate, endDate, reason, type, isRecurring, recurringDay)
        AddDayOffRequest request = new AddDayOffRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                "Sick Leave",
                "SICK",
                false,  // isRecurring
                null    // recurringDay
        );
        DoctorDayOffDto mockDto = new DoctorDayOffDto(dayOffId, LocalDate.now(), LocalDate.now().plusDays(2), "Sick Leave", "SICK", false, null);

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(doctorService.updateDayOff(eq(TEST_EMAIL), eq(dayOffId), any(AddDayOffRequest.class)))
                .thenReturn(mockDto);

        // Act
        ResponseEntity<DoctorDayOffDto> response =
                doctorScheduleController.updateDayOff(dayOffId, request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
        verify(doctorService).updateDayOff(TEST_EMAIL, dayOffId, request);
    }

    @Test
    void testDeleteDayOff() {
        // Arrange
        Long dayOffId = 1L;
        when(authentication.getName()).thenReturn(TEST_EMAIL);

        // Act
        ResponseEntity<Void> response =
                doctorScheduleController.deleteDayOff(dayOffId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(doctorService).deleteDayOff(TEST_EMAIL, dayOffId);
    }
}