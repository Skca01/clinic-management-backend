package com.amante.clinicmanagement.dto.request;

import com.amante.clinicmanagement.entity.DoctorDayOff;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddDayOffRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String reason;

    @NotBlank(message = "Type is required")
    private String type;

    private Boolean isRecurring = false;

    private String recurringDay;

    public void validate() {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be before or equal to end date"
            );
        }

        try {
            DoctorDayOff.DayOffType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid type. Must be HOLIDAY, VACATION, PERSONAL, or SICK"
            );
        }

        if (isRecurring && recurringDay != null) {
            try {
                DayOfWeek.valueOf(recurringDay);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid recurring day. Must be MONDAY, TUESDAY, etc."
                );
            }
        }
    }
}