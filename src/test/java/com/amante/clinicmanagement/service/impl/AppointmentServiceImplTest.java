package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.dto.request.BookAppointmentRequest;
import com.amante.clinicmanagement.dto.request.RejectAppointmentRequest;
import com.amante.clinicmanagement.dto.response.AppointmentDto;
import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.entity.Doctor;
import com.amante.clinicmanagement.entity.Patient;
import com.amante.clinicmanagement.entity.User;
import com.amante.clinicmanagement.repository.AppointmentRepository;
import com.amante.clinicmanagement.repository.DoctorRepository;
import com.amante.clinicmanagement.repository.PatientRepository;
import com.amante.clinicmanagement.repository.UserRepository;
import com.amante.clinicmanagement.service.AppointmentEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentServiceImpl Test Suite - 100% Coverage (Optimized)")
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentEmailService emailService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User patientUser;
    private User doctorUser;
    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private BookAppointmentRequest bookRequest;
    private RejectAppointmentRequest rejectRequest;

    @BeforeEach
    void setUp() {
        // Setup patient user
        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setEmail("patient@test.com");
        patientUser.setPassword("password");
        patientUser.setRole(User.Role.PATIENT);

        // Setup doctor user
        doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setPassword("password");
        doctorUser.setRole(User.Role.DOCTOR);

        // Setup patient
        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);
        patient.setFirstName("Kent");
        patient.setLastName("Carlo");
        patient.setPhone("+1234567890");
        patient.setGender("MALE");
        patient.setBirthDate(LocalDate.of(1990, 1, 1));

        // Setup doctor
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(doctorUser);
        doctor.setFirstName("Dr. Sarah");
        doctor.setLastName("Smith");
        doctor.setSpecialization("Cardiologist");
        doctor.setClinicAddress("123 Medical Plaza");
        doctor.setClinicCity("New York");
        doctor.setClinicCountry("USA");
        doctor.setConsultationFee(new BigDecimal("100.00"));

        // Setup appointment
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(LocalDateTime.now().plusDays(1));
        appointment.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setPatientNotes("Test notes");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        // Setup requests
        bookRequest = new BookAppointmentRequest();
        bookRequest.setDoctorId(1L);
        bookRequest.setStartTime(LocalDateTime.now().plusDays(1));
        bookRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        bookRequest.setPatientNotes("Test notes");

        rejectRequest = new RejectAppointmentRequest();
        rejectRequest.setRejectionReason("Emergency surgery scheduled at that time");
    }

    // ==================== bookAppointment Tests ====================

    @Test
    @DisplayName("Book appointment - Success and all error cases")
    void bookAppointment_AllScenarios() {
        // Success case
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsOverlappingAppointment(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.bookAppointment(bookRequest, "patient@test.com");
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(emailService).sendPendingAppointmentEmails(any(Appointment.class));

        // User not found
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.bookAppointment(bookRequest, "patient@test.com"));
        assertEquals("User not found", exception.getMessage());

        // Patient profile not found
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.bookAppointment(bookRequest, "patient@test.com"));
        assertTrue(exception.getMessage().contains("Patient profile not found"));

        // Doctor not found
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.bookAppointment(bookRequest, "patient@test.com"));
        assertEquals("Doctor not found", exception.getMessage());

        // Time slot already booked
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsOverlappingAppointment(anyLong(), any(), any())).thenReturn(true);
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.bookAppointment(bookRequest, "patient@test.com"));
        assertEquals("Time slot is already booked", exception.getMessage());
    }

    @Test
    @DisplayName("Book appointment - Email service failure")
    void bookAppointment_EmailServiceFailure() {
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsOverlappingAppointment(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doThrow(new RuntimeException("Email service error")).when(emailService)
                .sendPendingAppointmentEmails(any(Appointment.class));

        AppointmentDto result = appointmentService.bookAppointment(bookRequest, "patient@test.com");
        assertNotNull(result);
        verify(emailService).sendPendingAppointmentEmails(any(Appointment.class));
    }

    // ==================== confirmAppointment Tests ====================

    @Test
    @DisplayName("Confirm appointment - Success and all error cases")
    void confirmAppointment_AllScenarios() {
        // Success case
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.confirmAppointment(1L, "doctor@test.com");
        assertNotNull(result);
        verify(emailService).sendConfirmationEmail(any(Appointment.class));

        // User not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.confirmAppointment(1L, "doctor@test.com"));
        assertEquals("User not found", exception.getMessage());

        // Doctor profile not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.confirmAppointment(1L, "doctor@test.com"));
        assertTrue(exception.getMessage().contains("Doctor profile not found"));

        // Appointment not found
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.confirmAppointment(1L, "doctor@test.com"));
        assertEquals("Appointment not found", exception.getMessage());

        // Not authorized
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(999L);
        anotherDoctor.setUser(doctorUser);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(anotherDoctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.confirmAppointment(1L, "doctor@test.com"));
        assertEquals("You are not authorized to confirm this appointment", exception.getMessage());

        // Not pending status
        appointment.setStatus(Appointment.Status.CONFIRMED);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.confirmAppointment(1L, "doctor@test.com"));
        assertEquals("Only pending appointments can be confirmed", exception.getMessage());
    }

    @Test
    @DisplayName("Confirm appointment - Email service failure")
    void confirmAppointment_EmailServiceFailure() {
        appointment.setStatus(Appointment.Status.PENDING);
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doThrow(new RuntimeException("Email error")).when(emailService).sendConfirmationEmail(any());

        AppointmentDto result = appointmentService.confirmAppointment(1L, "doctor@test.com");
        assertNotNull(result);
        verify(emailService).sendConfirmationEmail(any(Appointment.class));
    }

    // ==================== rejectAppointment Tests ====================

    @Test
    @DisplayName("Reject appointment - Success and all error cases")
    void rejectAppointment_AllScenarios() {
        // Success case
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest);
        assertNotNull(result);
        verify(emailService).sendRejectionEmail(any(Appointment.class));

        // User not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest));
        assertEquals("User not found", exception.getMessage());

        // Doctor profile not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest));
        assertTrue(exception.getMessage().contains("Doctor profile not found"));

        // Appointment not found
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest));
        assertEquals("Appointment not found", exception.getMessage());

        // Not authorized
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(999L);
        anotherDoctor.setUser(doctorUser);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(anotherDoctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest));
        assertEquals("You are not authorized to reject this appointment", exception.getMessage());

        // Not pending status
        appointment.setStatus(Appointment.Status.CONFIRMED);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest));
        assertEquals("Only pending appointments can be rejected", exception.getMessage());
    }

    @Test
    @DisplayName("Reject appointment - Email service failure")
    void rejectAppointment_EmailServiceFailure() {
        appointment.setStatus(Appointment.Status.PENDING);
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doThrow(new RuntimeException("Email error")).when(emailService).sendRejectionEmail(any());

        AppointmentDto result = appointmentService.rejectAppointment(1L, "doctor@test.com", rejectRequest);
        assertNotNull(result);
        verify(emailService).sendRejectionEmail(any(Appointment.class));
    }

    // ==================== completeAppointment Tests ====================

    @Test
    @DisplayName("Complete appointment - Success and all error cases")
    void completeAppointment_AllScenarios() {
        // Success case
        appointment.setStatus(Appointment.Status.CONFIRMED);
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.completeAppointment(1L, "doctor@test.com");
        assertNotNull(result);
        verify(emailService).sendCompletionEmail(any(Appointment.class));

        // User not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.completeAppointment(1L, "doctor@test.com"));
        assertEquals("User not found", exception.getMessage());

        // Doctor profile not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.completeAppointment(1L, "doctor@test.com"));
        assertTrue(exception.getMessage().contains("Doctor profile not found"));

        // Appointment not found
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.completeAppointment(1L, "doctor@test.com"));
        assertEquals("Appointment not found", exception.getMessage());

        // Not authorized
        appointment.setStatus(Appointment.Status.CONFIRMED);
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(999L);
        anotherDoctor.setUser(doctorUser);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(anotherDoctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.completeAppointment(1L, "doctor@test.com"));
        assertEquals("You are not authorized to complete this appointment", exception.getMessage());

        // Not confirmed status
        appointment.setStatus(Appointment.Status.PENDING);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.completeAppointment(1L, "doctor@test.com"));
        assertEquals("Only confirmed appointments can be marked as completed", exception.getMessage());
    }

    @Test
    @DisplayName("Complete appointment - Email service failure")
    void completeAppointment_EmailServiceFailure() {
        appointment.setStatus(Appointment.Status.CONFIRMED);
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doThrow(new RuntimeException("Email error")).when(emailService).sendCompletionEmail(any());

        AppointmentDto result = appointmentService.completeAppointment(1L, "doctor@test.com");
        assertNotNull(result);
        verify(emailService).sendCompletionEmail(any(Appointment.class));
    }

    // ==================== getMyAppointments Tests ====================

    @Test
    @DisplayName("Get my appointments - Both roles with data and empty")
    void getMyAppointments_AllScenarios() {
        List<Appointment> appointments = Arrays.asList(appointment);

        // Patient role with data
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByPatientId(1L)).thenReturn(appointments);
        List<AppointmentDto> result = appointmentService.getMyAppointments("patient@test.com");
        assertEquals(1, result.size());

        // Patient role empty list
        when(appointmentRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
        result = appointmentService.getMyAppointments("patient@test.com");
        assertEquals(0, result.size());

        // Doctor role with data
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorId(1L)).thenReturn(appointments);
        result = appointmentService.getMyAppointments("doctor@test.com");
        assertEquals(1, result.size());

        // Doctor role empty list
        when(appointmentRepository.findByDoctorId(1L)).thenReturn(Collections.emptyList());
        result = appointmentService.getMyAppointments("doctor@test.com");
        assertEquals(0, result.size());

        // User not found
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getMyAppointments("unknown@test.com"));
        assertEquals("User not found", exception.getMessage());

        // Patient profile not found
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getMyAppointments("patient@test.com"));
        assertEquals("Patient profile not found", exception.getMessage());

        // Doctor profile not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getMyAppointments("doctor@test.com"));
        assertEquals("Doctor profile not found", exception.getMessage());

        // Invalid user role
        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(null);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getMyAppointments("admin@test.com"));
        assertEquals("Invalid user role", exception.getMessage());
    }

    // ==================== getDoctorPendingAppointments Tests ====================

    @Test
    @DisplayName("Get doctor pending appointments - Success and errors")
    void getDoctorPendingAppointments_AllScenarios() {
        List<Appointment> appointments = Arrays.asList(appointment);

        // Success with data
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorIdAndStatus(1L, Appointment.Status.PENDING))
                .thenReturn(appointments);
        List<AppointmentDto> result = appointmentService.getDoctorPendingAppointments("doctor@test.com");
        assertEquals(1, result.size());

        // Empty list
        when(appointmentRepository.findByDoctorIdAndStatus(1L, Appointment.Status.PENDING))
                .thenReturn(Collections.emptyList());
        result = appointmentService.getDoctorPendingAppointments("doctor@test.com");
        assertEquals(0, result.size());

        // User not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getDoctorPendingAppointments("doctor@test.com"));
        assertEquals("User not found", exception.getMessage());

        // Doctor profile not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getDoctorPendingAppointments("doctor@test.com"));
        assertTrue(exception.getMessage().contains("Doctor profile not found"));
    }

    // ==================== cancelAppointment Tests ====================

    @Test
    @DisplayName("Cancel appointment - Patient and Doctor success cases")
    void cancelAppointment_SuccessCases() {
        // Patient cancels successfully (PENDING status)
        appointment.setStatus(Appointment.Status.PENDING);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.cancelAppointment(1L, "patient@test.com");
        assertNotNull(result);
        verify(emailService).sendCancellationEmails(any(Appointment.class), eq("PATIENT"));

        // Doctor cancels successfully (reset to PENDING)
        appointment.setStatus(Appointment.Status.PENDING);
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        result = appointmentService.cancelAppointment(1L, "doctor@test.com");
        assertNotNull(result);
        verify(emailService).sendCancellationEmails(any(Appointment.class), eq("DOCTOR"));

        // Rejected status can be cancelled
        appointment.setStatus(Appointment.Status.REJECTED);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        result = appointmentService.cancelAppointment(1L, "patient@test.com");
        assertNotNull(result);

        // Confirmed status can be cancelled
        appointment.setStatus(Appointment.Status.CONFIRMED);
        result = appointmentService.cancelAppointment(1L, "patient@test.com");
        assertNotNull(result);
    }

    @Test
    @DisplayName("Cancel appointment - All error cases")
    void cancelAppointment_ErrorCases() {
        // User not found
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "unknown@test.com"));
        assertEquals("User not found", exception.getMessage());

        // Appointment not found
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "patient@test.com"));
        assertEquals("Appointment not found", exception.getMessage());

        // Already cancelled
        appointment.setStatus(Appointment.Status.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "patient@test.com"));
        assertEquals("Appointment is already cancelled", exception.getMessage());

        // Completed appointment
        appointment.setStatus(Appointment.Status.COMPLETED);
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "patient@test.com"));
        assertEquals("Cannot cancel completed appointment", exception.getMessage());

        // Patient not authorized
        appointment.setStatus(Appointment.Status.PENDING);
        Patient anotherPatient = new Patient();
        anotherPatient.setId(999L);
        anotherPatient.setUser(patientUser);
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(anotherPatient));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "patient@test.com"));
        assertEquals("You are not authorized to cancel this appointment", exception.getMessage());

        // Doctor not authorized
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(999L);
        anotherDoctor.setUser(doctorUser);
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(anotherDoctor));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "doctor@test.com"));
        assertEquals("You are not authorized to cancel this appointment", exception.getMessage());

        // Patient profile not found
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "patient@test.com"));
        assertEquals("Patient profile not found", exception.getMessage());

        // Doctor profile not found
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctorUser));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "doctor@test.com"));
        assertEquals("Doctor profile not found", exception.getMessage());

        // Invalid user role
        User invalidUser = new User();
        invalidUser.setId(3L);
        invalidUser.setEmail("invalid@test.com");
        invalidUser.setRole(null);
        when(userRepository.findByEmail("invalid@test.com")).thenReturn(Optional.of(invalidUser));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        exception = assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(1L, "invalid@test.com"));
        assertEquals("Invalid user role", exception.getMessage());
    }

    @Test
    @DisplayName("Cancel appointment - Email service failure")
    void cancelAppointment_EmailServiceFailure() {
        appointment.setStatus(Appointment.Status.PENDING);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doThrow(new RuntimeException("Email error")).when(emailService)
                .sendCancellationEmails(any(), anyString());

        AppointmentDto result = appointmentService.cancelAppointment(1L, "patient@test.com");
        assertNotNull(result);
        verify(emailService).sendCancellationEmails(any(Appointment.class), eq("PATIENT"));
    }

    // ==================== getAppointmentById Tests ====================

    @Test
    @DisplayName("Get appointment by ID - Success and not found")
    void getAppointmentById_AllScenarios() {
        // Success
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        AppointmentDto result = appointmentService.getAppointmentById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Not found
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.getAppointmentById(1L));
        assertEquals("Appointment not found", exception.getMessage());
    }

    // ==================== convertToDto Tests ====================

    @Test
    @DisplayName("Convert to DTO - All fields and variations")
    void convertToDto_AllScenarios() {
        // All fields populated
        appointment.setRejectionReason("Test rejection reason");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        AppointmentDto result = appointmentService.getAppointmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPatientId());
        assertEquals("Kent Carlo", result.getPatientName());
        assertEquals("patient@test.com", result.getPatientEmail());
        assertEquals("+1234567890", result.getPatientPhone());
        assertEquals("MALE", result.getPatientGender());
        assertEquals(1L, result.getDoctorId());
        assertEquals("Dr. Sarah Smith", result.getDoctorName());
        assertEquals("doctor@test.com", result.getDoctorEmail());
        assertEquals("Cardiologist", result.getDoctorSpecialization());
        assertEquals("123 Medical Plaza", result.getDoctorClinicAddress());
        assertEquals("New York", result.getDoctorClinicCity());
        assertEquals("USA", result.getDoctorClinicCountry());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Test notes", result.getPatientNotes());
        assertEquals("Test rejection reason", result.getRejectionReason());

        // Null optional fields
        appointment.setPatientNotes(null);
        appointment.setRejectionReason(null);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        result = appointmentService.getAppointmentById(1L);
        assertNull(result.getPatientNotes());
        assertNull(result.getRejectionReason());

        // Test all statuses
        Appointment.Status[] allStatuses = {
                Appointment.Status.PENDING,
                Appointment.Status.CONFIRMED,
                Appointment.Status.REJECTED,
                Appointment.Status.COMPLETED,
                Appointment.Status.CANCELLED
        };

        for (Appointment.Status status : allStatuses) {
            appointment.setStatus(status);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            result = appointmentService.getAppointmentById(1L);
            assertEquals(status.name(), result.getStatus());
        }
    }
}