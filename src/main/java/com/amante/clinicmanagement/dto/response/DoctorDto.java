package com.amante.clinicmanagement.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String bio;
    private BigDecimal consultationFee;
    private String currency;
    private String email;
    private String clinicCountry;
    private String clinicCity;
    private String clinicAddress;
    private String profilePictureUrl;
}