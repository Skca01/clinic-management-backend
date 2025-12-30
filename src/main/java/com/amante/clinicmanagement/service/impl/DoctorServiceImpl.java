package com.amante.clinicmanagement.service.impl;

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
import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.entity.Doctor;
import com.amante.clinicmanagement.entity.DoctorBreak;
import com.amante.clinicmanagement.entity.DoctorDayOff;
import com.amante.clinicmanagement.entity.DoctorSettings;
import com.amante.clinicmanagement.entity.DoctorWeeklySchedule;
import com.amante.clinicmanagement.entity.User;
import com.amante.clinicmanagement.repository.AppointmentRepository;
import com.amante.clinicmanagement.repository.DoctorBreakRepository;
import com.amante.clinicmanagement.repository.DoctorDayOffRepository;
import com.amante.clinicmanagement.repository.DoctorRepository;
import com.amante.clinicmanagement.repository.DoctorSettingsRepository;
import com.amante.clinicmanagement.repository.DoctorWeeklyScheduleRepository;
import com.amante.clinicmanagement.repository.UserRepository;
import com.amante.clinicmanagement.service.CloudinaryService;
import com.amante.clinicmanagement.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private static final String DOCTOR_NOT_FOUND = "Doctor not found";
    private static final int DEFAULT_SLOT_DURATION = 30;
    private static final int DEFAULT_BUFFER_TIME = 0;
    private static final String DEFAULT_TIMEZONE = "UTC";

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorSettingsRepository doctorSettingsRepository;
    private final DoctorWeeklyScheduleRepository weeklyScheduleRepository;
    private final DoctorBreakRepository doctorBreakRepository;
    private final DoctorDayOffRepository doctorDayOffRepository;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    private Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Doctor profile not found"));
    }

    @Override
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> searchDoctors(String country, String city,
                                         String specialization) {
        if ((country != null && !country.isEmpty()) ||
                (city != null && !city.isEmpty()) ||
                (specialization != null && !specialization.isEmpty())) {
            return doctorRepository.findByLocationAndSpecialization(
                            country, city, specialization)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return getAllDoctors();
    }

    @Override
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(DOCTOR_NOT_FOUND));
        return convertToDto(doctor);
    }

    @Override
    public List<TimeSlotDto> getAvailableSlots(Long doctorId,
                                               LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException(DOCTOR_NOT_FOUND));

        DoctorSettings settings = doctorSettingsRepository
                .findByDoctorId(doctorId)
                .orElse(new DoctorSettings(doctor, DEFAULT_SLOT_DURATION,
                        DEFAULT_BUFFER_TIME, DEFAULT_TIMEZONE));

        ZoneId doctorTimezone = ZoneId.of(settings.getTimezone());

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorDayOff> recurringDaysOff = doctorDayOffRepository
                .findRecurringByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        if (!recurringDaysOff.isEmpty()) {
            return Collections.emptyList();
        }

        List<DoctorDayOff> daysOff = doctorDayOffRepository
                .findByDoctorIdAndDate(doctorId, date);
        if (!daysOff.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<DoctorWeeklySchedule> scheduleOpt = weeklyScheduleRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        if (scheduleOpt.isEmpty() || Boolean.FALSE.equals(scheduleOpt.get().getIsAvailable())) {
            return Collections.emptyList();
        }

        DoctorWeeklySchedule schedule = scheduleOpt.get();
        LocalTime workStartTime = schedule.getStartTime();
        LocalTime workEndTime = schedule.getEndTime();

        int slotDuration = settings.getSlotDuration();
        int bufferTime = settings.getBufferTime();

        List<DoctorBreak> breaks = doctorBreakRepository
                .findByDoctorIdAndDay(doctorId, dayOfWeek.name());

        ZonedDateTime startOfDay = date.atTime(workStartTime)
                .atZone(doctorTimezone);
        ZonedDateTime endOfDay = date.atTime(workEndTime)
                .atZone(doctorTimezone);

        LocalDateTime startOfDayLocal = startOfDay.toLocalDateTime();
        LocalDateTime endOfDayLocal = endOfDay.toLocalDateTime();
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndDate(doctorId, startOfDayLocal,
                        endOfDayLocal);

        List<TimeSlotDto> slots = new ArrayList<>();
        ZonedDateTime currentSlot = startOfDay;

        while (currentSlot.isBefore(endOfDay)) {
            ZonedDateTime slotEnd = currentSlot.plusMinutes(slotDuration);
            if (slotEnd.isAfter(endOfDay)) {
                break;
            }

            boolean isDuringBreak = false;
            String breakReason = null;

            for (DoctorBreak breakTime : breaks) {
                ZonedDateTime breakStart = date
                        .atTime(breakTime.getStartTime())
                        .atZone(doctorTimezone);
                ZonedDateTime breakEnd = date
                        .atTime(breakTime.getEndTime())
                        .atZone(doctorTimezone);

                if (currentSlot.isBefore(breakEnd)
                        && slotEnd.isAfter(breakStart)) {
                    isDuringBreak = true;
                    breakReason = breakTime.getBreakName()
                            .toUpperCase(Locale.ROOT);
                    break;
                }
            }

            boolean isAvailable = true;
            String reason = null;

            if (isDuringBreak) {
                isAvailable = false;
                reason = breakReason;
            } else {
                LocalDateTime currentSlotLocal = currentSlot.toLocalDateTime();
                LocalDateTime slotEndLocal = slotEnd.toLocalDateTime();

                for (Appointment apt : bookedAppointments) {
                    if (!(currentSlotLocal.isAfter(apt.getEndTime()) ||
                            slotEndLocal.isBefore(apt.getStartTime()))) {
                        isAvailable = false;
                        reason = "BOOKED";
                        break;
                    }
                }
            }

            TimeSlotDto slot = new TimeSlotDto();
            slot.setStartTime(currentSlot.toLocalDateTime());
            slot.setEndTime(slotEnd.toLocalDateTime());
            slot.setAvailable(isAvailable);
            slot.setReason(reason);
            slots.add(slot);

            currentSlot = slotEnd.plusMinutes(bufferTime);
        }

        return slots;
    }

    @Override
    public DoctorScheduleResponse getDoctorSchedule(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException(DOCTOR_NOT_FOUND));
        return getScheduleForDoctorEntity(doctor);
    }

    @Override
    public DoctorScheduleResponse getDoctorSchedule(String email) {
        Doctor doctor = getDoctorByEmail(email);
        return getScheduleForDoctorEntity(doctor);
    }

    // Helper to avoid code duplication
    private DoctorScheduleResponse getScheduleForDoctorEntity(
            Doctor doctor) {
        DoctorSettings settings = doctorSettingsRepository
                .findByDoctorId(doctor.getId())
                .orElse(new DoctorSettings(doctor, DEFAULT_SLOT_DURATION,
                        DEFAULT_BUFFER_TIME, DEFAULT_TIMEZONE));

        List<DoctorWeeklySchedule> weeklySchedules =
                weeklyScheduleRepository.findByDoctorId(doctor.getId());
        List<DoctorBreak> breaks = doctorBreakRepository
                .findByDoctorId(doctor.getId());
        List<DoctorDayOff> daysOff = doctorDayOffRepository
                .findByDoctorId(doctor.getId());

        DoctorScheduleResponse response = new DoctorScheduleResponse();
        response.setSettings(convertSettingsToDto(settings));
        response.setWeeklySchedule(weeklySchedules.stream()
                .map(this::convertWeeklyScheduleToDto)
                .collect(Collectors.toList()));
        response.setBreaks(breaks.stream()
                .map(this::convertBreakToDto)
                .collect(Collectors.toList()));
        response.setDaysOff(daysOff.stream()
                .map(this::convertDayOffToDto)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public DoctorDto updateDoctorProfile(String email,
                                         UpdateDoctorProfileRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setBio(request.getBio());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setCurrency(request.getCurrency());
        doctor.setClinicCountry(request.getClinicCountry());
        doctor.setClinicCity(request.getClinicCity());
        doctor.setClinicAddress(request.getClinicAddress());

        return convertToDto(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public DoctorDto uploadProfilePicture(
            String email, MultipartFile file) throws IOException {
        Doctor doctor = getDoctorByEmail(email);

        if (doctor.getProfilePictureUrl() != null
                && !doctor.getProfilePictureUrl().isEmpty()) {
            try {
                cloudinaryService.deleteImage(
                        doctor.getProfilePictureUrl());
            } catch (IOException e) {
                log.error("Error deleting old profile picture: {}",
                        e.getMessage(), e);
            }
        }

        String imageUrl = cloudinaryService.uploadImage(file, "doctors");
        doctor.setProfilePictureUrl(imageUrl);

        return convertToDto(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public void deleteProfilePicture(String email) throws IOException {
        Doctor doctor = getDoctorByEmail(email);

        if (doctor.getProfilePictureUrl() != null
                && !doctor.getProfilePictureUrl().isEmpty()) {
            cloudinaryService.deleteImage(doctor.getProfilePictureUrl());
            doctor.setProfilePictureUrl(null);
            doctorRepository.save(doctor);
        }
    }

    @Override
    @Transactional
    public DoctorSettingsDto updateDoctorSettings(
            String email, UpdateDoctorSettingsRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorSettings settings = doctorSettingsRepository
                .findByDoctorId(doctor.getId())
                .orElse(new DoctorSettings(doctor, DEFAULT_SLOT_DURATION,
                        DEFAULT_BUFFER_TIME, DEFAULT_TIMEZONE));

        settings.setSlotDuration(request.getSlotDuration());
        settings.setBufferTime(request.getBufferTime());
        settings.setTimezone(request.getTimezone());

        return convertSettingsToDto(doctorSettingsRepository.save(settings));
    }

    @Override
    @Transactional
    public List<WeeklyScheduleDto> updateWeeklySchedule(
            String email, UpdateWeeklyScheduleRequest request) {
        Doctor doctor = getDoctorByEmail(email);
        List<DoctorWeeklySchedule> updatedSchedules = new ArrayList<>();

        for (Map.Entry<?, UpdateWeeklyScheduleRequest.DaySchedule> entry
                : request.getSchedule().entrySet()) {
            DayOfWeek day;
            if (entry.getKey() instanceof String) {
                day = DayOfWeek.valueOf(
                        ((String) entry.getKey()).toUpperCase(Locale.ROOT));
            } else {
                day = (DayOfWeek) entry.getKey();
            }

            UpdateWeeklyScheduleRequest.DaySchedule daySchedule =
                    entry.getValue();

            DoctorWeeklySchedule schedule = weeklyScheduleRepository
                    .findByDoctorIdAndDayOfWeek(doctor.getId(), day)
                    .orElse(new DoctorWeeklySchedule());

            schedule.setDoctor(doctor);
            schedule.setDayOfWeek(day);
            schedule.setIsAvailable(daySchedule.getAvailable());
            schedule.setStartTime(daySchedule.getStartTime());
            schedule.setEndTime(daySchedule.getEndTime());

            updatedSchedules.add(weeklyScheduleRepository.save(schedule));
        }

        return updatedSchedules.stream()
                .map(this::convertWeeklyScheduleToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorBreakDto addBreak(String email, AddBreakRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorBreak breakEntity = new DoctorBreak();
        breakEntity.setDoctor(doctor);
        breakEntity.setBreakName(request.getBreakName());
        breakEntity.setStartTime(request.getStartTime());
        breakEntity.setEndTime(request.getEndTime());
        breakEntity.setDayOfWeek(request.getDayOfWeek());

        return convertBreakToDto(doctorBreakRepository.save(breakEntity));
    }

    @Override
    @Transactional
    public DoctorBreakDto updateBreak(String email, Long breakId,
                                      AddBreakRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorBreak breakEntity = doctorBreakRepository.findById(breakId)
                .orElseThrow(() -> new RuntimeException("Break not found"));

        if (!breakEntity.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Break does not belong to this doctor");
        }

        breakEntity.setBreakName(request.getBreakName());
        breakEntity.setStartTime(request.getStartTime());
        breakEntity.setEndTime(request.getEndTime());
        breakEntity.setDayOfWeek(request.getDayOfWeek());

        return convertBreakToDto(doctorBreakRepository.save(breakEntity));
    }

    @Override
    @Transactional
    public void deleteBreak(String email, Long breakId) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorBreak breakEntity = doctorBreakRepository.findById(breakId)
                .orElseThrow(() -> new RuntimeException("Break not found"));

        if (!breakEntity.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Break does not belong to this doctor");
        }

        doctorBreakRepository.delete(breakEntity);
    }

    @Override
    @Transactional
    public DoctorDayOffDto addDayOff(String email,
                                     AddDayOffRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorDayOff dayOff = new DoctorDayOff();
        dayOff.setDoctor(doctor);
        dayOff.setStartDate(request.getStartDate());
        dayOff.setEndDate(request.getEndDate());
        dayOff.setReason(request.getReason());
        dayOff.setType(DoctorDayOff.DayOffType.valueOf(request.getType()));
        dayOff.setIsRecurring(request.getIsRecurring());

        if (request.getIsRecurring() && request.getRecurringDay() != null) {
            dayOff.setRecurringDay(
                    DayOfWeek.valueOf(request.getRecurringDay()));
        }

        return convertDayOffToDto(doctorDayOffRepository.save(dayOff));
    }

    @Override
    @Transactional
    public DoctorDayOffDto updateDayOff(String email, Long dayOffId,
                                        AddDayOffRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorDayOff dayOff = doctorDayOffRepository.findById(dayOffId)
                .orElseThrow(() -> new RuntimeException("Day off not found"));

        if (!dayOff.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException(
                    "Day off does not belong to this doctor");
        }

        dayOff.setStartDate(request.getStartDate());
        dayOff.setEndDate(request.getEndDate());
        dayOff.setReason(request.getReason());
        dayOff.setType(DoctorDayOff.DayOffType.valueOf(request.getType()));
        dayOff.setIsRecurring(request.getIsRecurring());

        if (request.getIsRecurring() && request.getRecurringDay() != null) {
            dayOff.setRecurringDay(
                    DayOfWeek.valueOf(request.getRecurringDay()));
        } else {
            dayOff.setRecurringDay(null);
        }

        return convertDayOffToDto(doctorDayOffRepository.save(dayOff));
    }

    @Override
    @Transactional
    public void deleteDayOff(String email, Long dayOffId) {
        Doctor doctor = getDoctorByEmail(email);

        DoctorDayOff dayOff = doctorDayOffRepository.findById(dayOffId)
                .orElseThrow(() -> new RuntimeException("Day off not found"));

        if (!dayOff.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException(
                    "Day off does not belong to this doctor");
        }

        doctorDayOffRepository.delete(dayOff);
    }

    private DoctorDto convertToDto(Doctor doctor) {
        return new DoctorDto(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                doctor.getBio(),
                doctor.getConsultationFee(),
                doctor.getCurrency(),
                doctor.getUser().getEmail(),
                doctor.getClinicCountry(),
                doctor.getClinicCity(),
                doctor.getClinicAddress(),
                doctor.getProfilePictureUrl()
        );
    }

    private DoctorSettingsDto convertSettingsToDto(
            DoctorSettings settings) {
        return new DoctorSettingsDto(
                settings.getId(),
                settings.getSlotDuration(),
                settings.getBufferTime(),
                settings.getTimezone()
        );
    }

    private WeeklyScheduleDto convertWeeklyScheduleToDto(
            DoctorWeeklySchedule schedule) {
        return new WeeklyScheduleDto(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getIsAvailable(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }

    private DoctorBreakDto convertBreakToDto(DoctorBreak breakEntity) {
        return new DoctorBreakDto(
                breakEntity.getId(),
                breakEntity.getDayOfWeek(),
                breakEntity.getBreakName(),
                breakEntity.getStartTime(),
                breakEntity.getEndTime()
        );
    }

    private DoctorDayOffDto convertDayOffToDto(DoctorDayOff dayOff) {
        String recurringDayName;
        if (dayOff.getRecurringDay() != null) {
            recurringDayName = dayOff.getRecurringDay().name();
        } else {
            recurringDayName = null;
        }

        return new DoctorDayOffDto(
                dayOff.getId(),
                dayOff.getStartDate(),
                dayOff.getEndDate(),
                dayOff.getReason(),
                dayOff.getType().name(),
                dayOff.getIsRecurring(),
                recurringDayName
        );
    }
}