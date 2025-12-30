package com.amante.clinicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddBreakRequest {

    @NotBlank(message = "Break name is required")
    private String breakName;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;

    public void validate() {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Break start time must be before end time"
            );
        }

        if (!dayOfWeek.equals("ALL")) {
            try {
                DayOfWeek.valueOf(dayOfWeek);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid day of week. Must be ALL, MONDAY, " +
                                "TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, " +
                                "SATURDAY, or SUNDAY"
                );
            }
        }
    }
}