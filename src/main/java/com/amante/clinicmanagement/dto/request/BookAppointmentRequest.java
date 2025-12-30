package com.amante.clinicmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to book an appointment")
public class BookAppointmentRequest {

    @NotNull(message = "Doctor ID is required")
    @Schema(
            description = "ID of the doctor",
            example = "1",
            required = true
    )
    private Long doctorId;

    @NotNull(message = "Start time is required")
    @Schema(
            description = "Appointment start time",
            example = "2025-12-25T14:00:00",
            required = true
    )
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(
            description = "Appointment end time",
            example = "2025-12-25T14:30:00",
            required = true
    )
    private LocalDateTime endTime;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(
            description = "Optional notes from patient",
            example = "I have been experiencing headaches for the past week"
    )
    private String patientNotes;
}