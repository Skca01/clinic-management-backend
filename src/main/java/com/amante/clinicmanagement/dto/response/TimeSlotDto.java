package com.amante.clinicmanagement.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TimeSlotDto {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean available;
    private String reason;
}