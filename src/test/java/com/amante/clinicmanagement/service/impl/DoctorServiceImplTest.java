package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.dto.request.AddBreakRequest;
import com.amante.clinicmanagement.dto.request.AddDayOffRequest;
import com.amante.clinicmanagement.dto.request.UpdateDoctorProfileRequest;
import com.amante.clinicmanagement.dto.request.UpdateDoctorSettingsRequest;
import com.amante.clinicmanagement.dto.request.UpdateWeeklyScheduleRequest;
import com.amante.clinicmanagement.dto.response.DoctorBreakDto;
import com.amante.clinicmanagement.dto.response.DoctorDayOffDto;
import com.amante.clinicmanagement.dto.response.DoctorDto;
import com.amante.clinicmanagement.dto.response.DoctorScheduleResponse;
import com.amante.clinicmanagement.dto.response.DoctorSettingsDto;
import com.amante.clinicmanagement.dto.response.TimeSlotDto;
import com.amante.clinicmanagement.dto.response.WeeklyScheduleDto;
import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.entity.Doctor;
import com.amante.clinicmanagement.entity.DoctorBreak;
import com.amante.clinicmanagement.entity.DoctorDayOff;
import com.amante.clinicmanagement.entity.DoctorSettings;
import com.amante.clinicmanagement.entity.DoctorWeeklySchedule;
import com.amante.clinicmanagement.entity.Patient;
import com.amante.clinicmanagement.entity.User;
import com.amante.clinicmanagement.repository.AppointmentRepository;
import com.amante.clinicmanagement.repository.DoctorBreakRepository;
import com.amante.clinicmanagement.repository.DoctorDayOffRepository;
import com.amante.clinicmanagement.repository.DoctorRepository;
import com.amante.clinicmanagement.repository.DoctorSettingsRepository;
import com.amante.clinicmanagement.repository.DoctorWeeklyScheduleRepository;
import com.amante.clinicmanagement.repository.UserRepository;
import com.amante.clinicmanagement.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorSettingsRepository doctorSettingsRepository;

    @Mock
    private DoctorWeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private DoctorBreakRepository doctorBreakRepository;

    @Mock
    private DoctorDayOffRepository doctorDayOffRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private User testUser;
    private Doctor testDoctor;
    private DoctorSettings testSettings;
    private DoctorWeeklySchedule testSchedule;
    private DoctorBreak testBreak;
    private DoctorDayOff testDayOff;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "doctor@test.com", "password",
                User.Role.DOCTOR, true, LocalDateTime.now());

        testDoctor = new Doctor(
                1L,
                testUser,
                "Kent",
                "Carlo",
                "Cardiologist",
                "Experienced doctor",
                new BigDecimal("1000.00"),
                "PHP",
                "09:00",
                "17:00",
                "Philippines",
                "Manila",
                "123 Medical St",
                "http://image.url/profile.jpg"
        );

        testSettings = new DoctorSettings(testDoctor, 30, 0, "Asia/Manila");
        testSettings.setId(1L);

        testSchedule = new DoctorWeeklySchedule(
                testDoctor,
                DayOfWeek.MONDAY,
                true,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
        testSchedule.setId(1L);

        testBreak = new DoctorBreak(
                testDoctor,
                "MONDAY",
                "Lunch Break",
                LocalTime.of(12, 0),
                LocalTime.of(13, 0)
        );
        testBreak.setId(1L);

        testDayOff = new DoctorDayOff(
                testDoctor,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "Holiday",
                DoctorDayOff.DayOffType.HOLIDAY
        );
        testDayOff.setId(1L);
        testDayOff.setIsRecurring(false);
    }

    // ==================== GET ALL DOCTORS TESTS ====================

    @Test
    void testGetAllDoctors_ReturnsListOrEmpty() {
        // Test with data
        when(doctorRepository.findAll()).thenReturn(List.of(testDoctor));
        List<DoctorDto> result = doctorService.getAllDoctors();
        assertNotNull(result);
        assertEquals(1, result.size());

        // Test empty list
        when(doctorRepository.findAll()).thenReturn(Collections.emptyList());
        result = doctorService.getAllDoctors();
        assertTrue(result.isEmpty());

        verify(doctorRepository, times(2)).findAll();
    }

    // ==================== SEARCH DOCTORS TESTS ====================

    @ParameterizedTest
    @CsvSource({
            "Philippines,,,true",
            ",Manila,,true",
            ",,Cardiologist,true",
            "Philippines,Manila,Cardiologist,true",
            ",,,false",
            "'','','',false"
    })
    void testSearchDoctors_WithVariousParameters(String country, String city, String specialization, boolean shouldSearch) {
        List<Doctor> doctors = List.of(testDoctor);

        if (shouldSearch) {
            when(doctorRepository.findByLocationAndSpecialization(country, city, specialization))
                    .thenReturn(doctors);
        } else {
            when(doctorRepository.findAll()).thenReturn(doctors);
        }

        List<DoctorDto> result = doctorService.searchDoctors(country, city, specialization);

        assertNotNull(result);
        assertEquals(1, result.size());

        if (shouldSearch) {
            verify(doctorRepository).findByLocationAndSpecialization(country, city, specialization);
        } else {
            verify(doctorRepository).findAll();
        }
    }

    // ==================== GET DOCTOR BY ID TESTS ====================

    @Test
    void testGetDoctorById_FoundAndNotFound() {
        // Test found
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        DoctorDto result = doctorService.getDoctorById(1L);
        assertNotNull(result);
        assertEquals("Kent", result.getFirstName());

        // Test not found
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.getDoctorById(999L);
        });
        assertEquals("Doctor not found", exception.getMessage());
    }

    // ==================== GET AVAILABLE SLOTS TESTS ====================

    @Test
    void testGetAvailableSlots_RecurringAndSpecificDayOff_ReturnsEmpty() {
        LocalDate testDate = LocalDate.of(2025, 1, 6);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));

        // Test recurring day off
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(testDayOff));
        List<TimeSlotDto> result = doctorService.getAvailableSlots(1L, testDate);
        assertTrue(result.isEmpty());

        // Test specific date day off
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorIdAndDate(1L, testDate)).thenReturn(List.of(testDayOff));
        result = doctorService.getAvailableSlots(1L, testDate);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableSlots_NoScheduleOrNotAvailable_ReturnsEmpty() {
        LocalDate testDate = LocalDate.of(2025, 1, 6);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorIdAndDate(any(), any()))
                .thenReturn(Collections.emptyList());

        // Test no schedule
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());
        List<TimeSlotDto> result = doctorService.getAvailableSlots(1L, testDate);
        assertTrue(result.isEmpty());

        // Test schedule not available
        testSchedule.setIsAvailable(false);
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        result = doctorService.getAvailableSlots(1L, testDate);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableSlots_WithBreaksAndAppointments_MarksUnavailable() {
        LocalDate testDate = LocalDate.of(2025, 1, 6);
        testSchedule.setIsAvailable(true);

        Patient patient = new Patient();
        patient.setId(1L);

        Appointment bookedAppointment = new Appointment(
                1L, patient, testDoctor,
                LocalDateTime.of(2025, 1, 6, 10, 0),
                LocalDateTime.of(2025, 1, 6, 10, 30),
                Appointment.Status.CONFIRMED, "Notes", null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorIdAndDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(doctorBreakRepository.findByDoctorIdAndDay(1L, "MONDAY"))
                .thenReturn(List.of(testBreak));
        when(appointmentRepository.findByDoctorIdAndDate(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(bookedAppointment));

        List<TimeSlotDto> result = doctorService.getAvailableSlots(1L, testDate);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(slot -> !slot.getAvailable() && "LUNCH BREAK".equals(slot.getReason())));
        assertTrue(result.stream().anyMatch(slot -> !slot.getAvailable() && "BOOKED".equals(slot.getReason())));
    }

    @Test
    void testGetAvailableSlots_NoSettingsAndWithBufferTime_Works() {
        LocalDate testDate = LocalDate.of(2025, 1, 6);
        testSchedule.setIsAvailable(true);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorIdAndDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(doctorBreakRepository.findByDoctorIdAndDay(any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByDoctorIdAndDate(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Test with no settings (uses defaults)
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        List<TimeSlotDto> result = doctorService.getAvailableSlots(1L, testDate);
        assertFalse(result.isEmpty());

        // Test with buffer time
        testSettings.setBufferTime(15);
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        result = doctorService.getAvailableSlots(1L, testDate);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAvailableSlots_AppointmentBoundaries_CorrectAvailability() {
        LocalDate testDate = LocalDate.of(2025, 1, 6);
        testSchedule.setIsAvailable(true);

        Patient patient = new Patient();
        patient.setId(1L);

        // Appointment from 9:00-9:30
        Appointment earlyAppointment = new Appointment(
                1L, patient, testDoctor,
                LocalDateTime.of(2025, 1, 6, 9, 0),
                LocalDateTime.of(2025, 1, 6, 9, 30),
                Appointment.Status.CONFIRMED, "Notes", null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorIdAndDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(doctorBreakRepository.findByDoctorIdAndDay(any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByDoctorIdAndDate(any(), any(), any()))
                .thenReturn(List.of(earlyAppointment));

        List<TimeSlotDto> result = doctorService.getAvailableSlots(1L, testDate);

        // Slot at appointment end time (9:30) should be unavailable
        Optional<TimeSlotDto> slot930 = result.stream()
                .filter(slot -> slot.getStartTime().equals(LocalDateTime.of(2025, 1, 6, 9, 30)))
                .findFirst();
        assertTrue(slot930.isPresent());
        assertFalse(slot930.get().getAvailable());

        // Slot after appointment (10:00) should be available
        Optional<TimeSlotDto> slot1000 = result.stream()
                .filter(slot -> slot.getStartTime().equals(LocalDateTime.of(2025, 1, 6, 10, 0)))
                .findFirst();
        assertTrue(slot1000.isPresent());
        assertTrue(slot1000.get().getAvailable());
    }

    @Test
    void testGetAvailableSlots_SlotOverrunsEndTime_ReturnsEmpty() {
        LocalDate testDate = LocalDate.of(2025, 1, 6);

        // Short shift: 9:00 to 9:20 (20 minutes) but 30-minute slots
        testSchedule.setStartTime(LocalTime.of(9, 0));
        testSchedule.setEndTime(LocalTime.of(9, 20));
        testSchedule.setIsAvailable(true);
        testSettings.setSlotDuration(30);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        when(doctorDayOffRepository.findRecurringByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorIdAndDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(testSchedule));
        when(doctorBreakRepository.findByDoctorIdAndDay(any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByDoctorIdAndDate(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<TimeSlotDto> result = doctorService.getAvailableSlots(1L, testDate);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableSlots_DoctorNotFound_ThrowsException() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.getAvailableSlots(999L, LocalDate.of(2025, 1, 6));
        });

        assertEquals("Doctor not found", exception.getMessage());
    }

    // ==================== GET DOCTOR SCHEDULE TESTS ====================

    @Test
    void testGetDoctorSchedule_ByIdAndEmail_WithAndWithoutSettings() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(weeklyScheduleRepository.findByDoctorId(1L)).thenReturn(List.of(testSchedule));
        when(doctorBreakRepository.findByDoctorId(1L)).thenReturn(List.of(testBreak));
        when(doctorDayOffRepository.findByDoctorId(1L)).thenReturn(List.of(testDayOff));

        // Test with settings
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        DoctorScheduleResponse result = doctorService.getDoctorSchedule(1L);
        assertNotNull(result);
        assertEquals(30, result.getSettings().getSlotDuration());

        // Test without settings (creates default)
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        result = doctorService.getDoctorSchedule(1L);
        assertNotNull(result.getSettings());
        assertEquals(30, result.getSettings().getSlotDuration());
        assertEquals("UTC", result.getSettings().getTimezone());

        // Test by email
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        result = doctorService.getDoctorSchedule("doctor@test.com");
        assertNotNull(result);
        verify(userRepository).findByEmail("doctor@test.com");
    }

    @Test
    void testGetDoctorSchedule_DoctorNotFound_ThrowsException() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.getDoctorSchedule(999L);
        });

        assertEquals("Doctor not found", exception.getMessage());
    }

    @Test
    void testGetDoctorSchedule_WithRecurringDay_ConvertsCorrectly() {
        testDayOff.setRecurringDay(DayOfWeek.MONDAY);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        when(weeklyScheduleRepository.findByDoctorId(1L)).thenReturn(Collections.emptyList());
        when(doctorBreakRepository.findByDoctorId(1L)).thenReturn(Collections.emptyList());
        when(doctorDayOffRepository.findByDoctorId(1L)).thenReturn(List.of(testDayOff));

        DoctorScheduleResponse result = doctorService.getDoctorSchedule(1L);
        assertEquals("MONDAY", result.getDaysOff().get(0).getRecurringDay());

        // Test without recurring day
        testDayOff.setRecurringDay(null);
        result = doctorService.getDoctorSchedule(1L);
        assertNull(result.getDaysOff().get(0).getRecurringDay());
    }

    // ==================== UPDATE DOCTOR PROFILE TESTS ====================

    @Test
    void testUpdateDoctorProfile_SuccessAndErrors() {
        UpdateDoctorProfileRequest request = new UpdateDoctorProfileRequest(
                "Jane", "Smith", "Neurologist", "New bio",
                new BigDecimal("2000.00"), "USD", "USA", "New York", "789 Ave"
        );

        // Test success
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);
        DoctorDto result = doctorService.updateDoctorProfile("doctor@test.com", request);
        assertNotNull(result);

        // Test user not found
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorProfile("notfound@test.com", request);
        });
        assertEquals("User not found", exception.getMessage());

        // Test doctor not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorProfile("doctor@test.com", request);
        });
        assertEquals("Doctor profile not found", exception.getMessage());
    }

    // ==================== UPLOAD PROFILE PICTURE TESTS ====================

    @Test
    void testUploadProfilePicture_WithAndWithoutExisting() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String newImageUrl = "http://image.url/new-profile.jpg";

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(cloudinaryService.uploadImage(file, "doctors")).thenReturn(newImageUrl);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Test with existing picture (should delete old)
        DoctorDto result = doctorService.uploadProfilePicture("doctor@test.com", file);
        assertNotNull(result);
        verify(cloudinaryService).deleteImage("http://image.url/profile.jpg");

        // Test without existing picture (null or empty)
        testDoctor.setProfilePictureUrl(null);
        result = doctorService.uploadProfilePicture("doctor@test.com", file);
        assertNotNull(result);

        testDoctor.setProfilePictureUrl("");
        result = doctorService.uploadProfilePicture("doctor@test.com", file);
        assertNotNull(result);

        // Should only delete once (first test)
        verify(cloudinaryService, times(1)).deleteImage(anyString());
    }

    @Test
    void testUploadProfilePicture_DeleteFailsContinuesUpload() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String newImageUrl = "http://image.url/new-profile.jpg";

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        doThrow(new IOException("Delete failed")).when(cloudinaryService)
                .deleteImage("http://image.url/profile.jpg");
        when(cloudinaryService.uploadImage(file, "doctors")).thenReturn(newImageUrl);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        DoctorDto result = doctorService.uploadProfilePicture("doctor@test.com", file);
        assertNotNull(result);
        verify(cloudinaryService).uploadImage(file, "doctors");
    }

    // ==================== DELETE PROFILE PICTURE TESTS ====================

    @Test
    void testDeleteProfilePicture_WithAndWithoutExisting() throws IOException {
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Test with existing picture
        doctorService.deleteProfilePicture("doctor@test.com");
        verify(cloudinaryService).deleteImage("http://image.url/profile.jpg");
        verify(doctorRepository).save(any(Doctor.class));

        // Test without existing picture (null)
        testDoctor.setProfilePictureUrl(null);
        doctorService.deleteProfilePicture("doctor@test.com");
        verify(cloudinaryService, times(1)).deleteImage(anyString()); // Still only 1 from before

        // Test with empty URL
        testDoctor.setProfilePictureUrl("");
        doctorService.deleteProfilePicture("doctor@test.com");
        verify(cloudinaryService, times(1)).deleteImage(anyString()); // Still only 1
    }

    // ==================== UPDATE DOCTOR SETTINGS TESTS ====================

    @Test
    void testUpdateDoctorSettings_WithAndWithoutExisting() {
        UpdateDoctorSettingsRequest request = new UpdateDoctorSettingsRequest(
                45, 15, "America/New_York"
        );

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorSettingsRepository.save(any(DoctorSettings.class))).thenReturn(testSettings);

        // Test with existing settings
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.of(testSettings));
        DoctorSettingsDto result = doctorService.updateDoctorSettings("doctor@test.com", request);
        assertNotNull(result);

        // Test without existing settings
        when(doctorSettingsRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        result = doctorService.updateDoctorSettings("doctor@test.com", request);
        assertNotNull(result);

        verify(doctorSettingsRepository, times(2)).save(any(DoctorSettings.class));
    }

    // ==================== UPDATE WEEKLY SCHEDULE TESTS ====================

    @Test
    void testUpdateWeeklySchedule_WithAndWithoutExisting() {
        Map<DayOfWeek, UpdateWeeklyScheduleRequest.DaySchedule> scheduleMap = new HashMap<>();
        scheduleMap.put(DayOfWeek.MONDAY, new UpdateWeeklyScheduleRequest.DaySchedule(
                true, LocalTime.of(8, 0), LocalTime.of(16, 0)
        ));
        UpdateWeeklyScheduleRequest request = new UpdateWeeklyScheduleRequest(scheduleMap);

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(weeklyScheduleRepository.save(any(DoctorWeeklySchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Test with existing schedule
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        List<WeeklyScheduleDto> result = doctorService.updateWeeklySchedule("doctor@test.com", request);
        assertEquals(1, result.size());

        // Test without existing schedule
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());
        result = doctorService.updateWeeklySchedule("doctor@test.com", request);
        assertEquals(1, result.size());

        verify(weeklyScheduleRepository, times(2)).save(any(DoctorWeeklySchedule.class));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testUpdateWeeklySchedule_WithStringKeys_ConvertsToEnum() {
        Map rawMap = new HashMap();
        rawMap.put("MONDAY", new UpdateWeeklyScheduleRequest.DaySchedule(
                true, LocalTime.of(8, 0), LocalTime.of(16, 0)
        ));
        UpdateWeeklyScheduleRequest request = new UpdateWeeklyScheduleRequest(rawMap);

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(weeklyScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(weeklyScheduleRepository.save(any(DoctorWeeklySchedule.class))).thenReturn(testSchedule);

        List<WeeklyScheduleDto> result = doctorService.updateWeeklySchedule("doctor@test.com", request);
        assertNotNull(result);
        verify(weeklyScheduleRepository).findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY);
    }

    // ==================== BREAK CRUD TESTS ====================

    @Test
    void testAddBreak_Success() {
        AddBreakRequest request = new AddBreakRequest(
                "Coffee Break", LocalTime.of(15, 0), LocalTime.of(15, 15), "MONDAY"
        );

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorBreakRepository.save(any(DoctorBreak.class))).thenReturn(testBreak);

        DoctorBreakDto result = doctorService.addBreak("doctor@test.com", request);
        assertNotNull(result);
        verify(doctorBreakRepository).save(any(DoctorBreak.class));
    }

    @Test
    void testUpdateBreak_SuccessAndErrors() {
        AddBreakRequest request = new AddBreakRequest(
                "Extended Lunch", LocalTime.of(12, 0), LocalTime.of(13, 30), "MONDAY"
        );

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));

        // Test success
        when(doctorBreakRepository.findById(1L)).thenReturn(Optional.of(testBreak));
        when(doctorBreakRepository.save(any(DoctorBreak.class))).thenReturn(testBreak);
        DoctorBreakDto result = doctorService.updateBreak("doctor@test.com", 1L, request);
        assertNotNull(result);

        // Test break not found
        when(doctorBreakRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateBreak("doctor@test.com", 999L, request);
        });
        assertEquals("Break not found", exception.getMessage());

        // Test wrong doctor
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        testBreak.setDoctor(anotherDoctor);
        when(doctorBreakRepository.findById(1L)).thenReturn(Optional.of(testBreak));
        exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateBreak("doctor@test.com", 1L, request);
        });
        assertEquals("Break does not belong to this doctor", exception.getMessage());
    }

    @Test
    void testDeleteBreak_SuccessAndErrors() {
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));

        // Test success
        when(doctorBreakRepository.findById(1L)).thenReturn(Optional.of(testBreak));
        doctorService.deleteBreak("doctor@test.com", 1L);
        verify(doctorBreakRepository).delete(testBreak);

        // Test not found
        when(doctorBreakRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.deleteBreak("doctor@test.com", 999L);
        });
        assertEquals("Break not found", exception.getMessage());

        // Test wrong doctor
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        testBreak.setDoctor(anotherDoctor);
        when(doctorBreakRepository.findById(1L)).thenReturn(Optional.of(testBreak));
        exception = assertThrows(RuntimeException.class, () -> {
            doctorService.deleteBreak("doctor@test.com", 1L);
        });
        assertEquals("Break does not belong to this doctor", exception.getMessage());
    }

    // ==================== DAY OFF CRUD TESTS ====================

    @Test
    void testAddDayOff_WithRecurringVariations() {
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorDayOffRepository.save(any(DoctorDayOff.class))).thenReturn(testDayOff);

        // Test with recurring
        AddDayOffRequest request1 = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(7), "Vacation", "VACATION", true, "MONDAY"
        );
        DoctorDayOffDto result = doctorService.addDayOff("doctor@test.com", request1);
        assertNotNull(result);

        // Test recurring true but null day
        AddDayOffRequest request2 = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(7), "Vacation", "VACATION", true, null
        );
        result = doctorService.addDayOff("doctor@test.com", request2);
        assertNotNull(result);

        // Test recurring false with day specified
        AddDayOffRequest request3 = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(1), "Sick", "SICK", false, "MONDAY"
        );
        result = doctorService.addDayOff("doctor@test.com", request3);
        assertNotNull(result);

        // Test without recurring
        AddDayOffRequest request4 = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(1), "Sick", "SICK", false, null
        );
        result = doctorService.addDayOff("doctor@test.com", request4);
        assertNotNull(result);

        verify(doctorDayOffRepository, times(4)).save(any(DoctorDayOff.class));
    }

    @Test
    void testUpdateDayOff_SuccessAndErrors() {
        AddDayOffRequest request = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(5), "Updated", "VACATION", true, "FRIDAY"
        );

        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));

        // Test success
        when(doctorDayOffRepository.findById(1L)).thenReturn(Optional.of(testDayOff));
        when(doctorDayOffRepository.save(any(DoctorDayOff.class))).thenReturn(testDayOff);
        DoctorDayOffDto result = doctorService.updateDayOff("doctor@test.com", 1L, request);
        assertNotNull(result);

        // Test with recurring variations
        AddDayOffRequest request2 = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(5), "Updated", "VACATION", true, null
        );
        result = doctorService.updateDayOff("doctor@test.com", 1L, request2);
        assertNotNull(result);

        AddDayOffRequest request3 = new AddDayOffRequest(
                LocalDate.now(), LocalDate.now().plusDays(1), "No recurring", "PERSONAL", false, "MONDAY"
        );
        result = doctorService.updateDayOff("doctor@test.com", 1L, request3);
        assertNotNull(result);

        // Test not found
        when(doctorDayOffRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDayOff("doctor@test.com", 999L, request);
        });
        assertEquals("Day off not found", exception.getMessage());

        // Test wrong doctor
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        testDayOff.setDoctor(anotherDoctor);
        when(doctorDayOffRepository.findById(1L)).thenReturn(Optional.of(testDayOff));
        exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDayOff("doctor@test.com", 1L, request);
        });
        assertEquals("Day off does not belong to this doctor", exception.getMessage());
    }

    @Test
    void testDeleteDayOff_SuccessAndErrors() {
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));

        // Test success
        when(doctorDayOffRepository.findById(1L)).thenReturn(Optional.of(testDayOff));
        doctorService.deleteDayOff("doctor@test.com", 1L);
        verify(doctorDayOffRepository).delete(testDayOff);

        // Test not found
        when(doctorDayOffRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.deleteDayOff("doctor@test.com", 999L);
        });
        assertEquals("Day off not found", exception.getMessage());

        // Test wrong doctor
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        testDayOff.setDoctor(anotherDoctor);
        when(doctorDayOffRepository.findById(1L)).thenReturn(Optional.of(testDayOff));
        exception = assertThrows(RuntimeException.class, () -> {
            doctorService.deleteDayOff("doctor@test.com", 1L);
        });
        assertEquals("Day off does not belong to this doctor", exception.getMessage());
    }
}