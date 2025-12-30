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
import com.amante.clinicmanagement.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String APPOINTMENT_NOT_FOUND = "Appointment not found";

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentEmailService emailService;

    private Patient getPatientByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Patient profile not found. " +
                                "Please complete your profile first."
                ));
    }

    private Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Doctor profile not found. " +
                                "Please complete your profile first."
                ));
    }

    @Override
    @Transactional
    public AppointmentDto bookAppointment(
            BookAppointmentRequest request,
            String patientEmail
    ) {
        Patient patient = getPatientByEmail(patientEmail);
        log.info(
                "=== Booking appointment for patient: {} ===",
                patient.getUser().getEmail()
        );

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean hasOverlap = appointmentRepository.existsOverlappingAppointment(
                doctor.getId(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (hasOverlap) {
            throw new RuntimeException("Time slot is already booked");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setPatientNotes(request.getPatientNotes());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        log.info(
                "✓ Appointment created with PENDING status, ID: {}",
                appointment.getId()
        );

        try {
            emailService.sendPendingAppointmentEmails(appointment);
        } catch (RuntimeException e) {
            log.error("Failed to send pending appointment emails", e);
        }

        return convertToDto(appointment);
    }

    @Override
    @Transactional
    public AppointmentDto confirmAppointment(
            Long appointmentId,
            String doctorEmail
    ) {
        Doctor doctor = getDoctorByEmail(doctorEmail);
        log.info(
                "=== Confirming appointment ID: {} by doctor: {} ===",
                appointmentId,
                doctor.getUser().getEmail()
        );

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(APPOINTMENT_NOT_FOUND)
                );

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException(
                    "You are not authorized to confirm this appointment"
            );
        }

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new RuntimeException(
                    "Only pending appointments can be confirmed"
            );
        }

        appointment.setStatus(Appointment.Status.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        try {
            emailService.sendConfirmationEmail(appointment);
        } catch (RuntimeException e) {
            log.error("Failed to send confirmation email", e);
        }

        return convertToDto(appointment);
    }

    @Override
    @Transactional
    public AppointmentDto rejectAppointment(
            Long appointmentId,
            String doctorEmail,
            RejectAppointmentRequest request
    ) {
        Doctor doctor = getDoctorByEmail(doctorEmail);
        log.info(
                "=== Rejecting appointment ID: {} by doctor: {} ===",
                appointmentId,
                doctor.getUser().getEmail()
        );

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(APPOINTMENT_NOT_FOUND)
                );

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException(
                    "You are not authorized to reject this appointment"
            );
        }

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new RuntimeException(
                    "Only pending appointments can be rejected"
            );
        }

        appointment.setStatus(Appointment.Status.REJECTED);
        appointment.setRejectionReason(request.getRejectionReason());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        try {
            emailService.sendRejectionEmail(appointment);
        } catch (RuntimeException e) {
            log.error("Failed to send rejection email", e);
        }

        return convertToDto(appointment);
    }

    @Override
    @Transactional
    public AppointmentDto completeAppointment(
            Long appointmentId,
            String doctorEmail
    ) {
        Doctor doctor = getDoctorByEmail(doctorEmail);
        log.info(
                "=== Completing appointment ID: {} by doctor: {} ===",
                appointmentId,
                doctor.getUser().getEmail()
        );

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(APPOINTMENT_NOT_FOUND)
                );

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException(
                    "You are not authorized to complete this appointment"
            );
        }

        if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
            throw new RuntimeException(
                    "Only confirmed appointments can be marked as completed"
            );
        }

        appointment.setStatus(Appointment.Status.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        try {
            emailService.sendCompletionEmail(appointment);
        } catch (RuntimeException e) {
            log.error("Failed to send completion email", e);
        }

        return convertToDto(appointment);
    }

    @Override
    public List<AppointmentDto> getMyAppointments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Patient profile not found")
                    );

            return appointmentRepository.findByPatientId(patient.getId())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (user.getRole() == User.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Doctor profile not found")
                    );

            return appointmentRepository.findByDoctorId(doctor.getId())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        throw new RuntimeException("Invalid user role");
    }

    @Override
    public List<AppointmentDto> getDoctorPendingAppointments(
            String doctorEmail
    ) {
        Doctor doctor = getDoctorByEmail(doctorEmail);
        return appointmentRepository.findByDoctorIdAndStatus(
                        doctor.getId(),
                        Appointment.Status.PENDING
                )
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentDto cancelAppointment(
            Long appointmentId,
            String userEmail
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        log.info(
                "=== Cancelling appointment ID: {} by user: {} (Role: {}) ===",
                appointmentId,
                userEmail,
                user.getRole()
        );

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(APPOINTMENT_NOT_FOUND)
                );

        if (appointment.getStatus() == Appointment.Status.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed appointment");
        }

        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Patient profile not found")
                    );

            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new RuntimeException(
                        "You are not authorized to cancel this appointment"
                );
            }
        } else {
            if (user.getRole() == User.Role.DOCTOR) {
                Doctor doctor = doctorRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException("Doctor profile not found")
                        );

                if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                    throw new RuntimeException(
                            "You are not authorized to cancel this appointment"
                    );
                }
            } else {
                throw new RuntimeException("Invalid user role");
            }
        }

        appointment.setStatus(Appointment.Status.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        try {
            emailService.sendCancellationEmails(
                    appointment,
                    user.getRole().name()
            );
        } catch (RuntimeException e) {
            log.error("Failed to send cancellation emails", e);
        }

        return convertToDto(appointment);
    }

    @Override
    public AppointmentDto getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(APPOINTMENT_NOT_FOUND)
                );

        return convertToDto(appointment);
    }

    private AppointmentDto convertToDto(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(
                appointment.getPatient().getFirstName()
                        + " "
                        + appointment.getPatient().getLastName()
        );
        dto.setPatientEmail(appointment.getPatient().getUser().getEmail());
        dto.setPatientPhone(appointment.getPatient().getPhone());
        dto.setPatientGender(appointment.getPatient().getGender());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(
                appointment.getDoctor().getFirstName()
                        + " "
                        + appointment.getDoctor().getLastName()
        );
        dto.setDoctorEmail(appointment.getDoctor().getUser().getEmail());
        dto.setDoctorSpecialization(
                appointment.getDoctor().getSpecialization()
        );
        dto.setDoctorClinicAddress(
                appointment.getDoctor().getClinicAddress()
        );
        dto.setDoctorClinicCity(appointment.getDoctor().getClinicCity());
        dto.setDoctorClinicCountry(
                appointment.getDoctor().getClinicCountry()
        );
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus().name());
        dto.setPatientNotes(appointment.getPatientNotes());
        dto.setRejectionReason(appointment.getRejectionReason());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }
}