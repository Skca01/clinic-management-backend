package com.amante.clinicmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reject an appointment")
public class RejectAppointmentRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(
            min = 10,
            max = 500,
            message = "Reason must be between 10 and 500 characters"
    )
    @Schema(
            description = "Reason for rejecting the appointment",
            example = "Unfortunately, I have an emergency surgery " +
                    "scheduled at that time. Please choose another slot.",
            required = true
    )
    private String rejectionReason;
}