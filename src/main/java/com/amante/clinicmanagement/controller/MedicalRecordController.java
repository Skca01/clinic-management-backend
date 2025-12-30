package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.DiagnosisRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.entity.MedicalRecord;
import com.amante.clinicmanagement.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(
        name = "Medical Records",
        description = "Medical record and diagnosis management endpoints"
)
@SecurityRequirement(name = "Bearer Authentication")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(
            summary = "Add diagnosis to appointment (Doctor only)"
    )
    public ResponseEntity<ApiResponse<MedicalRecord>> addDiagnosis(
            @Valid @RequestBody DiagnosisRequest request,
            Authentication authentication
    ) {
        MedicalRecord record =
                medicalRecordService.addDiagnosis(
                        request,
                        authentication.getName()
                );

        ApiResponse<MedicalRecord> apiResponse =
                new ApiResponse<>();

        apiResponse.setSuccess(true);
        apiResponse.setMessage(
                "Diagnosis added successfully"
        );
        apiResponse.setData(record);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(
            summary = "Get medical record by appointment ID"
    )
    public ResponseEntity<ApiResponse<MedicalRecord>> getMedicalRecord(
            @PathVariable Long appointmentId
    ) {
        MedicalRecord record =
                medicalRecordService
                        .getMedicalRecordByAppointmentId(
                                appointmentId
                        );

        ApiResponse<MedicalRecord> apiResponse =
                new ApiResponse<>();

        apiResponse.setSuccess(true);
        apiResponse.setMessage(
                "Medical record retrieved successfully"
        );
        apiResponse.setData(record);

        return ResponseEntity.ok(apiResponse);
    }
}