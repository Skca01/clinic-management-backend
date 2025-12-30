package com.amante.clinicmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSettingsDto {

    private Long id;
    private Integer slotDuration;
    private Integer bufferTime;
    private String timezone;
}