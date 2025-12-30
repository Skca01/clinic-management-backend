package com.amante.clinicmanagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Appointment information")
public class AppointmentDto {

    @Schema(description = "Appointment ID", example = "1")
    private Long id;

    @Schema(description = "Patient ID", example = "1")
    private Long patientId;

    @Schema(description = "Patient full name", example = "Kent Carlo")
    private String patientName;

    @Schema(description = "Patient email", example = "kent.doe@email.com")
    private String patientEmail;

    @Schema(description = "Patient phone", example = "+1234567890")
    private String patientPhone;

    @Schema(description = "Patient gender", example = "MALE")
    private String patientGender;

    @Schema(description = "Doctor ID", example = "1")
    private Long doctorId;

    @Schema(description = "Doctor full name", example = "Dr. Sarah Smith")
    private String doctorName;

    @Schema(description = "Doctor email", example = "dr.smith@clinic.com")
    private String doctorEmail;

    @Schema(description = "Doctor specialization", example = "Cardiologist")
    private String doctorSpecialization;

    @Schema(
            description = "Doctor clinic address",
            example = "123 Medical Plaza"
    )
    private String doctorClinicAddress;

    @Schema(description = "Doctor clinic city", example = "New York")
    private String doctorClinicCity;

    @Schema(description = "Doctor clinic country", example = "USA")
    private String doctorClinicCountry;

    @Schema(
            description = "Appointment start time",
            example = "2025-12-25T14:00:00"
    )
    private LocalDateTime startTime;

    @Schema(
            description = "Appointment end time",
            example = "2025-12-25T14:30:00"
    )
    private LocalDateTime endTime;

    @Schema(description = "Appointment status", example = "PENDING")
    private String status;

    @Schema(description = "Patient notes", example = "Having headaches")
    private String patientNotes;

    @Schema(
            description = "Rejection reason (if rejected)",
            example = "Emergency surgery scheduled"
    )
    private String rejectionReason;

    @Schema(description = "Created timestamp", example = "2025-12-20T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2025-12-20T10:05:00")
    private LocalDateTime updatedAt;
}