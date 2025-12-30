package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.UpdateDoctorProfileRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.dto.response.DoctorDto;
import com.amante.clinicmanagement.dto.response.TimeSlotDto;
import com.amante.clinicmanagement.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(
        name = "Doctors",
        description = "Doctor search and availability endpoints"
)
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Search doctors")
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getAllDoctors(
            @RequestParam(required = false)
            String specialization,
            @RequestParam(required = false)
            String country,
            @RequestParam(required = false)
            String city
    ) {
        List<DoctorDto> doctors =
                doctorService.searchDoctors(
                        country,
                        city,
                        specialization
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Doctors retrieved successfully",
                        doctors
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(
            @PathVariable
            Long id
    ) {
        DoctorDto doctor =
                doctorService.getDoctorById(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Doctor retrieved successfully",
                        doctor
                )
        );
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get doctor availability")
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> getAvailability(
            @PathVariable
            Long id,
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        List<TimeSlotDto> slots =
                doctorService.getAvailableSlots(
                        id,
                        date
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Available slots retrieved successfully",
                        slots
                )
        );
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update my professional profile",
            description =
                    "Updates the logged-in doctor's profile."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<DoctorDto>> updateDoctorProfile(
            @Valid
            @RequestBody
            UpdateDoctorProfileRequest request,
            Authentication authentication
    ) {
        DoctorDto doctor =
                doctorService.updateDoctorProfile(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Profile updated successfully",
                        doctor
                )
        );
    }

    @PostMapping(
            value = "/profile-picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload my profile picture")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<DoctorDto>> uploadProfilePicture(
            @RequestParam("file")
            MultipartFile file,
            Authentication authentication
    ) throws IOException {
        DoctorDto doctor =
                doctorService.uploadProfilePicture(
                        authentication.getName(),
                        file
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Profile picture uploaded successfully",
                        doctor
                )
        );
    }

    @DeleteMapping("/profile-picture")
    @Operation(summary = "Delete my profile picture")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePicture(
            Authentication authentication
    ) throws IOException {
        doctorService.deleteProfilePicture(
                authentication.getName()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Profile picture deleted successfully",
                        null
                )
        );
    }
}
