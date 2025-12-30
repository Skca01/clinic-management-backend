package com.amante.clinicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

public class UpdateWeeklyScheduleRequest {

    @NotNull(message = "Schedule map is required")
    private Map<DayOfWeek, DaySchedule> schedule;

    public static class DaySchedule {
        @NotNull(message = "Available status is required")
        private Boolean available;

        private LocalTime startTime;
        private LocalTime endTime;

        public DaySchedule() {}

        public DaySchedule(
                Boolean available,
                LocalTime startTime,
                LocalTime endTime
        ) {
            this.available = available;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }

        public void validate() {
            if (available != null && available) {
                if (startTime == null || endTime == null) {
                    throw new IllegalArgumentException(
                            "Start time and end time are required " +
                                    "when day is available"
                    );
                }
                if (!startTime.isBefore(endTime)) {
                    throw new IllegalArgumentException(
                            "Start time must be before end time"
                    );
                }
            }
        }
    }

    public UpdateWeeklyScheduleRequest() {}

    public UpdateWeeklyScheduleRequest(Map<DayOfWeek, DaySchedule> schedule) {
        this.schedule = schedule;
    }

    public Map<DayOfWeek, DaySchedule> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<DayOfWeek, DaySchedule> schedule) {
        this.schedule = schedule;
    }

    public void validate() {
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalArgumentException("Schedule cannot be empty");
        }
        schedule.values().forEach(DaySchedule::validate);
    }
}