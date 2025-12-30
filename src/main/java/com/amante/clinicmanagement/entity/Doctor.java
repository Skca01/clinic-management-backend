package com.amante.clinicmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "currency", length = 3)
    private String currency = "PHP";

    @Column(name = "working_start_time")
    private String workingStartTime = "09:00";

    @Column(name = "working_end_time")
    private String workingEndTime = "17:00";

    @Column(name = "clinic_country")
    private String clinicCountry;

    @Column(name = "clinic_city")
    private String clinicCity;

    @Column(name = "clinic_address", columnDefinition = "TEXT")
    private String clinicAddress;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    public Doctor(
            Long id,
            User user,
            String firstName,
            String lastName,
            String specialization,
            String bio,
            BigDecimal consultationFee,
            String currency,
            String workingStartTime,
            String workingEndTime,
            String clinicCountry,
            String clinicCity,
            String clinicAddress,
            String profilePictureUrl
    ) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.bio = bio;
        this.consultationFee = consultationFee;
        this.currency = currency;
        this.workingStartTime = workingStartTime;
        this.workingEndTime = workingEndTime;
        this.clinicCountry = clinicCountry;
        this.clinicCity = clinicCity;
        this.clinicAddress = clinicAddress;
        this.profilePictureUrl = profilePictureUrl;
    }
}