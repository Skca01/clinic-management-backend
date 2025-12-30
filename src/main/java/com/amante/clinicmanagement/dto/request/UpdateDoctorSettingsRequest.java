package com.amante.clinicmanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorSettingsRequest {

    public static final int SLOT_15 = 15;
    public static final int SLOT_30 = 30;
    public static final int SLOT_45 = 45;
    public static final int SLOT_60 = 60;

    public static final int BUFFER_MIN = 0;
    public static final int BUFFER_MAX = 30;

    @NotNull(message = "Slot duration is required")
    @Min(value = SLOT_15, message = "Slot duration must be at least 15 minutes")
    @Max(value = SLOT_60, message = "Slot duration must not exceed 60 minutes")
    private Integer slotDuration;

    @NotNull(message = "Buffer time is required")
    @Min(value = BUFFER_MIN, message = "Buffer time cannot be negative")
    @Max(value = BUFFER_MAX, message = "Buffer time must not exceed 30 minutes")
    private Integer bufferTime;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    public void validate() {
        if (slotDuration != SLOT_15 && slotDuration != SLOT_30 &&
                slotDuration != SLOT_45 && slotDuration != SLOT_60) {
            throw new IllegalArgumentException(
                    "Slot duration must be 15, 30, 45, or 60 minutes"
            );
        }

        if (bufferTime < BUFFER_MIN || bufferTime > BUFFER_MAX) {
            throw new IllegalArgumentException(
                    "Buffer time must be between 0 and 30 minutes"
            );
        }
    }
}