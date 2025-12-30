package com.amante.clinicmanagement.service;

import com.amante.clinicmanagement.dto.request.DiagnosisRequest;
import com.amante.clinicmanagement.entity.MedicalRecord;

public interface MedicalRecordService {

    MedicalRecord addDiagnosis(
            DiagnosisRequest request,
            String doctorEmail
    );

    MedicalRecord getMedicalRecordByAppointmentId(Long appointmentId);
}