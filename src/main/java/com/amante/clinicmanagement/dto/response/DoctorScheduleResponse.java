package com.amante.clinicmanagement.dto.response;

import java.util.List;

public class DoctorScheduleResponse {
    private DoctorSettingsDto settings;
    private List<WeeklyScheduleDto> weeklySchedule;
    private List<DoctorBreakDto> breaks;
    private List<DoctorDayOffDto> daysOff;

    public DoctorScheduleResponse() {
    }

    public DoctorScheduleResponse(DoctorSettingsDto settings,
                                  List<WeeklyScheduleDto> weeklySchedule,
                                  List<DoctorBreakDto> breaks,
                                  List<DoctorDayOffDto> daysOff) {
        this.settings = settings;
        this.weeklySchedule = weeklySchedule;
        this.breaks = breaks;
        this.daysOff = daysOff;
    }

    public DoctorSettingsDto getSettings() {
        return settings;
    }

    public void setSettings(DoctorSettingsDto settings) {
        this.settings = settings;
    }

    public List<WeeklyScheduleDto> getWeeklySchedule() {
        return weeklySchedule;
    }

    public void setWeeklySchedule(List<WeeklyScheduleDto> weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
    }

    public List<DoctorBreakDto> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<DoctorBreakDto> breaks) {
        this.breaks = breaks;
    }

    public List<DoctorDayOffDto> getDaysOff() {
        return daysOff;
    }

    public void setDaysOff(List<DoctorDayOffDto> daysOff) {
        this.daysOff = daysOff;
    }
}