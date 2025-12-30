package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.AddBreakRequest;
import com.amante.clinicmanagement.dto.request.AddDayOffRequest;
import com.amante.clinicmanagement.dto.request.UpdateDoctorSettingsRequest;
import com.amante.clinicmanagement.dto.request.UpdateWeeklyScheduleRequest;
import com.amante.clinicmanagement.dto.response.DoctorBreakDto;
import com.amante.clinicmanagement.dto.response.DoctorDayOffDto;
import com.amante.clinicmanagement.dto.response.DoctorScheduleResponse;
import com.amante.clinicmanagement.dto.response.DoctorSettingsDto;
import com.amante.clinicmanagement.dto.response.TimeSlotDto;
import com.amante.clinicmanagement.dto.response.WeeklyScheduleDto;
import com.amante.clinicmanagement.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

@RestController
@RequestMapping("/api/doctors/schedule")
@RequiredArgsConstructor
@Tag(
        name = "Doctor Schedule",
        description =
                "Doctor schedule configuration and management endpoints"
)
@SecurityRequirement(name = "Bearer Authentication")
public class DoctorScheduleController {

    private final DoctorService doctorService;

    @GetMapping("/config")
    @Operation(
            summary = "Get my schedule configuration",
            description =
                    "Retrieve my complete schedule settings, hours, and breaks."
    )
    public ResponseEntity<DoctorScheduleResponse> getMySchedule(
            Authentication authentication
    ) {
        DoctorScheduleResponse schedule =
                doctorService.getDoctorSchedule(
                        authentication.getName()
                );

        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{doctorId}/available-slots")
    @Operation(
            summary =
                    "Get available time slots for booking (Public)"
    )
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlots(
            @PathVariable
            Long doctorId,
            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        List<TimeSlotDto> slots =
                doctorService.getAvailableSlots(
                        doctorId,
                        date
                );

        return ResponseEntity.ok(slots);
    }

    @PutMapping("/settings")
    @Operation(summary = "Update my consultation settings")
    public ResponseEntity<DoctorSettingsDto> updateSettings(
            @Valid
            @RequestBody
            UpdateDoctorSettingsRequest request,
            Authentication authentication
    ) {
        DoctorSettingsDto updated =
                doctorService.updateDoctorSettings(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(updated);
    }

    @PutMapping("/weekly")
    @Operation(summary = "Update my weekly schedule")
    public ResponseEntity<List<WeeklyScheduleDto>> updateWeeklySchedule(
            @Valid
            @RequestBody
            UpdateWeeklyScheduleRequest request,
            Authentication authentication
    ) {
        List<WeeklyScheduleDto> updated =
                doctorService.updateWeeklySchedule(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/breaks")
    @Operation(summary = "Add a break to my schedule")
    public ResponseEntity<DoctorBreakDto> addBreak(
            @Valid
            @RequestBody
            AddBreakRequest request,
            Authentication authentication
    ) {
        DoctorBreakDto created =
                doctorService.addBreak(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(created);
    }

    @PutMapping("/breaks/{breakId}")
    @Operation(summary = "Update a break")
    public ResponseEntity<DoctorBreakDto> updateBreak(
            @PathVariable
            Long breakId,
            @Valid
            @RequestBody
            AddBreakRequest request,
            Authentication authentication
    ) {
        DoctorBreakDto updated =
                doctorService.updateBreak(
                        authentication.getName(),
                        breakId,
                        request
                );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/breaks/{breakId}")
    @Operation(summary = "Delete a break")
    public ResponseEntity<Void> deleteBreak(
            @PathVariable
            Long breakId,
            Authentication authentication
    ) {
        doctorService.deleteBreak(
                authentication.getName(),
                breakId
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/days-off")
    @Operation(summary = "Add a day off")
    public ResponseEntity<DoctorDayOffDto> addDayOff(
            @Valid
            @RequestBody
            AddDayOffRequest request,
            Authentication authentication
    ) {
        DoctorDayOffDto created =
                doctorService.addDayOff(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(created);
    }

    @PutMapping("/days-off/{dayOffId}")
    @Operation(summary = "Update a day off")
    public ResponseEntity<DoctorDayOffDto> updateDayOff(
            @PathVariable
            Long dayOffId,
            @Valid
            @RequestBody
            AddDayOffRequest request,
            Authentication authentication
    ) {
        DoctorDayOffDto updated =
                doctorService.updateDayOff(
                        authentication.getName(),
                        dayOffId,
                        request
                );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/days-off/{dayOffId}")
    @Operation(summary = "Delete a day off")
    public ResponseEntity<Void> deleteDayOff(
            @PathVariable
            Long dayOffId,
            Authentication authentication
    ) {
        doctorService.deleteDayOff(
                authentication.getName(),
                dayOffId
        );

        return ResponseEntity.noContent().build();
    }
}
