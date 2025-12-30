package com.amante.clinicmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to confirm an appointment")
public class ConfirmAppointmentRequest {

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(
            description = "Optional notes from doctor",
            example = "Please arrive 10 minutes early for registration"
    )
    private String doctorNotes;
}
