package com.amante.clinicmanagement.dto.response;

import java.time.LocalDate;

public class DoctorDayOffDto {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String type;
    private Boolean isRecurring;
    private String recurringDay;

    public DoctorDayOffDto() {
    }

    public DoctorDayOffDto(
            Long id,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            String type,
            Boolean isRecurring,
            String recurringDay
    ) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.type = type;
        this.isRecurring = isRecurring;
        this.recurringDay = recurringDay;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public String getRecurringDay() {
        return recurringDay;
    }

    public void setRecurringDay(String recurringDay) {
        this.recurringDay = recurringDay;
    }
}