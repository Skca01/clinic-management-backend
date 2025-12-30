package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.BookAppointmentRequest;
import com.amante.clinicmanagement.dto.request.RejectAppointmentRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.dto.response.AppointmentDto;
import com.amante.clinicmanagement.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(
        name = "Appointments",
        description = "Appointment booking and management endpoints"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Request a new appointment")
    public ResponseEntity<ApiResponse<AppointmentDto>> bookAppointment(
            @Valid
            @RequestBody
            BookAppointmentRequest request,
            Authentication authentication
    ) {
        AppointmentDto appointment =
                appointmentService.bookAppointment(
                        request,
                        authentication.getName()
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointment request submitted!",
                        appointment
                )
        );
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm pending appointment (Doctor only)")
    public ResponseEntity<ApiResponse<AppointmentDto>> confirmAppointment(
            @PathVariable
            Long id,
            Authentication authentication
    ) {
        AppointmentDto appointment =
                appointmentService.confirmAppointment(
                        id,
                        authentication.getName()
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointment confirmed!",
                        appointment
                )
        );
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject pending appointment (Doctor only)")
    public ResponseEntity<ApiResponse<AppointmentDto>> rejectAppointment(
            @PathVariable
            Long id,
            @Valid
            @RequestBody
            RejectAppointmentRequest request,
            Authentication authentication
    ) {
        AppointmentDto appointment =
                appointmentService.rejectAppointment(
                        id,
                        authentication.getName(),
                        request
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointment rejected.",
                        appointment
                )
        );
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark appointment as completed (Doctor only)")
    public ResponseEntity<ApiResponse<AppointmentDto>> completeAppointment(
            @PathVariable
            Long id,
            Authentication authentication
    ) {
        AppointmentDto appointment =
                appointmentService.completeAppointment(
                        id,
                        authentication.getName()
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointment completed.",
                        appointment
                )
        );
    }

    @GetMapping("/my-appointments")
    @Operation(summary = "Get my appointments")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>>
    getMyAppointments(Authentication authentication) {
        List<AppointmentDto> appointments =
                appointmentService.getMyAppointments(
                        authentication.getName()
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointments retrieved.",
                        appointments
                )
        );
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending appointments (Doctor only)")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>>
    getPendingAppointments(Authentication authentication) {
        List<AppointmentDto> appointments =
                appointmentService.getDoctorPendingAppointments(
                        authentication.getName()
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Pending appointments retrieved.",
                        appointments
                )
        );
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(
            @PathVariable
            Long id,
            Authentication authentication
    ) {
        AppointmentDto appointment =
                appointmentService.cancelAppointment(
                        id,
                        authentication.getName()
                );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointment cancelled.",
                        appointment
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<ApiResponse<AppointmentDto>> getAppointmentById(
            @PathVariable
            Long id
    ) {
        AppointmentDto appointment =
                appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Appointment retrieved.",
                        appointment
                )
        );
    }
}