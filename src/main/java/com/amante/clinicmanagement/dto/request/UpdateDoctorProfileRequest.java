package com.amante.clinicmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(
            min = 2,
            max = 50,
            message = "First name must be between 2 and 50 characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(
            min = 2,
            max = 50,
            message = "Last name must be between 2 and 50 characters"
    )
    private String lastName;

    @NotBlank(message = "Specialization is required")
    @Size(
            min = 2,
            max = 100,
            message = "Specialization must be between 2 and 100 characters"
    )
    private String specialization;

    @Size(
            max = 1000,
            message = "Biography must not exceed 1000 characters"
    )
    private String bio;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Consultation fee must be greater than 0"
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Invalid consultation fee format"
    )
    private BigDecimal consultationFee;

    @NotBlank(message = "Currency is required")
    @Size(
            min = 3,
            max = 3,
            message = "Currency must be a 3-letter code (e.g., PHP, USD)"
    )
    private String currency;

    @Size(
            max = 100,
            message = "Country must not exceed 100 characters"
    )
    private String clinicCountry;

    @Size(
            max = 100,
            message = "City must not exceed 100 characters"
    )
    private String clinicCity;

    @Size(
            max = 500,
            message = "Clinic address must not exceed 500 characters"
    )
    private String clinicAddress;
}