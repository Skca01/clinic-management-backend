package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.dto.request.DiagnosisRequest;
import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.entity.Doctor;
import com.amante.clinicmanagement.entity.MedicalRecord;
import com.amante.clinicmanagement.entity.User;
import com.amante.clinicmanagement.repository.AppointmentRepository;
import com.amante.clinicmanagement.repository.DoctorRepository;
import com.amante.clinicmanagement.repository.MedicalRecordRepository;
import com.amante.clinicmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private User testUser;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private DiagnosisRequest testDiagnosisRequest;
    private MedicalRecord testMedicalRecord;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("doctor@clinic.com");

        // Setup test doctor
        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setUser(testUser);

        // Setup test appointment
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setStatus(Appointment.Status.PENDING);

        // Setup test diagnosis request
        testDiagnosisRequest = new DiagnosisRequest();
        testDiagnosisRequest.setAppointmentId(1L);
        testDiagnosisRequest.setDiagnosis("Common cold");
        testDiagnosisRequest.setPrescription("Rest and fluids");

        // Setup test medical record
        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setId(1L);
        testMedicalRecord.setAppointment(testAppointment);
        testMedicalRecord.setDiagnosis("Common cold");
        testMedicalRecord.setPrescription("Rest and fluids");
        testMedicalRecord.setCreatedAt(LocalDateTime.now());
    }

    // =============== addDiagnosis Tests ===============

    @Test
    void addDiagnosis_Success() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findById(testDiagnosisRequest.getAppointmentId()))
                .thenReturn(Optional.of(testAppointment));
        when(medicalRecordRepository.save(any(MedicalRecord.class)))
                .thenReturn(testMedicalRecord);

        // Act
        MedicalRecord result = medicalRecordService.addDiagnosis(
                testDiagnosisRequest,
                testUser.getEmail()
        );

        // Assert
        assertNotNull(result);
        assertEquals(testMedicalRecord.getDiagnosis(), result.getDiagnosis());
        assertEquals(testMedicalRecord.getPrescription(), result.getPrescription());
        assertEquals(Appointment.Status.COMPLETED, testAppointment.getStatus());

        verify(userRepository).findByEmail(testUser.getEmail());
        verify(doctorRepository).findByUserId(testUser.getId());
        verify(appointmentRepository).findById(testDiagnosisRequest.getAppointmentId());
        verify(appointmentRepository).save(testAppointment);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void addDiagnosis_AllValidationErrors() {
        // Test 1: User not found
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                medicalRecordService.addDiagnosis(testDiagnosisRequest, testUser.getEmail())
        );
        assertEquals("User not found", exception.getMessage());

        // Test 2: Doctor profile not found
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(doctorRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.empty());

        exception = assertThrows(RuntimeException.class, () ->
                medicalRecordService.addDiagnosis(testDiagnosisRequest, testUser.getEmail())
        );
        assertEquals("Doctor profile not found", exception.getMessage());

        // Test 3: Appointment not found
        when(doctorRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findById(testDiagnosisRequest.getAppointmentId()))
                .thenReturn(Optional.empty());

        exception = assertThrows(RuntimeException.class, () ->
                medicalRecordService.addDiagnosis(testDiagnosisRequest, testUser.getEmail())
        );
        assertEquals("Appointment not found", exception.getMessage());

        // Test 4: Doctor not owner of appointment
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        testAppointment.setDoctor(anotherDoctor);
        when(appointmentRepository.findById(testDiagnosisRequest.getAppointmentId()))
                .thenReturn(Optional.of(testAppointment));

        exception = assertThrows(RuntimeException.class, () ->
                medicalRecordService.addDiagnosis(testDiagnosisRequest, testUser.getEmail())
        );
        assertEquals("You can only add diagnosis for your own appointments", exception.getMessage());

        // Test 5: Cancelled appointment
        testAppointment.setDoctor(testDoctor); // Reset to correct doctor
        testAppointment.setStatus(Appointment.Status.CANCELLED);

        exception = assertThrows(RuntimeException.class, () ->
                medicalRecordService.addDiagnosis(testDiagnosisRequest, testUser.getEmail())
        );
        assertEquals("Cannot add diagnosis to cancelled appointment", exception.getMessage());

        // Verify no saves occurred for any error case
        verify(appointmentRepository, never()).save(any());
        verify(medicalRecordRepository, never()).save(any());
    }

    // =============== getMedicalRecordByAppointmentId Tests ===============

    @Test
    void getMedicalRecordByAppointmentId_SuccessAndNotFound() {
        // Test 1: Success
        Long appointmentId = 1L;
        when(medicalRecordRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(testMedicalRecord));

        MedicalRecord result = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);

        assertNotNull(result);
        assertEquals(testMedicalRecord.getId(), result.getId());
        assertEquals(testMedicalRecord.getDiagnosis(), result.getDiagnosis());
        assertEquals(testMedicalRecord.getPrescription(), result.getPrescription());

        // Test 2: Not found
        Long notFoundId = 999L;
        when(medicalRecordRepository.findByAppointmentId(notFoundId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                medicalRecordService.getMedicalRecordByAppointmentId(notFoundId)
        );
        assertEquals("Medical record not found", exception.getMessage());

        // Verify calls
        verify(medicalRecordRepository).findByAppointmentId(appointmentId);
        verify(medicalRecordRepository).findByAppointmentId(notFoundId);
    }
}