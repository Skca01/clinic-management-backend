package com.amante.clinicmanagement.dto.response;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorBreakDto {

    private Long id;
    private String dayOfWeek;
    private String breakName;
    private LocalTime startTime;
    private LocalTime endTime;
}