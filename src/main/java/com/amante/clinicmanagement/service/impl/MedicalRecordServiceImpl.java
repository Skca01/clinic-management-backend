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
import com.amante.clinicmanagement.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    private Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Doctor profile not found"
                ));
    }

    @Override
    @Transactional
    public MedicalRecord addDiagnosis(
            DiagnosisRequest request,
            String doctorEmail
    ) {
        Doctor doctor = getDoctorByEmail(doctorEmail);

        Appointment appointment = appointmentRepository.findById(
                        request.getAppointmentId()
                )
                .orElseThrow(() -> new RuntimeException(
                        "Appointment not found"
                ));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException(
                    "You can only add diagnosis for your own appointments"
            );
        }

        if (appointment.getStatus() == Appointment.Status.CANCELLED) {
            throw new RuntimeException(
                    "Cannot add diagnosis to cancelled appointment"
            );
        }

        appointment.setStatus(Appointment.Status.COMPLETED);
        appointmentRepository.save(appointment);

        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setDiagnosis(request.getDiagnosis());
        record.setPrescription(request.getPrescription());
        record.setCreatedAt(LocalDateTime.now());

        return medicalRecordRepository.save(record);
    }

    @Override
    public MedicalRecord getMedicalRecordByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException(
                        "Medical record not found"
                ));
    }
}