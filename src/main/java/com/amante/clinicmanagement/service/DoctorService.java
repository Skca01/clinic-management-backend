package com.amante.clinicmanagement.service;

import com.amante.clinicmanagement.dto.request.AddBreakRequest;
import com.amante.clinicmanagement.dto.request.AddDayOffRequest;
import com.amante.clinicmanagement.dto.request.UpdateDoctorProfileRequest;
import com.amante.clinicmanagement.dto.request.UpdateDoctorSettingsRequest;
import com.amante.clinicmanagement.dto.request.UpdateWeeklyScheduleRequest;
import com.amante.clinicmanagement.dto.response.DoctorBreakDto;
import com.amante.clinicmanagement.dto.response.DoctorDayOffDto;
import com.amante.clinicmanagement.dto.response.DoctorDto;
import com.amante.clinicmanagement.dto.response.DoctorScheduleResponse;
import com.amante.clinicmanagement.dto.response.DoctorSettingsDto;
import com.amante.clinicmanagement.dto.response.TimeSlotDto;
import com.amante.clinicmanagement.dto.response.WeeklyScheduleDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface DoctorService {

    List<DoctorDto> getAllDoctors();

    List<DoctorDto> searchDoctors(
            String country,
            String city,
            String specialization
    );

    DoctorDto getDoctorById(Long id);

    List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date);

    DoctorScheduleResponse getDoctorSchedule(Long doctorId);

    DoctorScheduleResponse getDoctorSchedule(String email);

    DoctorDto updateDoctorProfile(
            String email,
            UpdateDoctorProfileRequest request
    );

    DoctorDto uploadProfilePicture(
            String email,
            MultipartFile file
    ) throws IOException;

    void deleteProfilePicture(String email) throws IOException;

    DoctorSettingsDto updateDoctorSettings(
            String email,
            UpdateDoctorSettingsRequest request
    );

    List<WeeklyScheduleDto> updateWeeklySchedule(
            String email,
            UpdateWeeklyScheduleRequest request
    );

    DoctorBreakDto addBreak(String email, AddBreakRequest request);

    DoctorBreakDto updateBreak(
            String email,
            Long breakId,
            AddBreakRequest request
    );

    void deleteBreak(String email, Long breakId);

    DoctorDayOffDto addDayOff(String email, AddDayOffRequest request);

    DoctorDayOffDto updateDayOff(
            String email,
            Long dayOffId,
            AddDayOffRequest request
    );

    void deleteDayOff(String email, Long dayOffId);
}