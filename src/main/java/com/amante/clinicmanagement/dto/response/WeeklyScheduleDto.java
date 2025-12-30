package com.amante.clinicmanagement.dto.response;

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
public class WeeklyScheduleDto {

    private Long id;
    private DayOfWeek dayOfWeek;
    private Boolean isAvailable;
    private LocalTime startTime;
    private LocalTime endTime;
}