package com.amante.clinicmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_settings")
public class DoctorSettings {

    private static final int DEFAULT_SLOT_DURATION = 30;

    private static final int DEFAULT_BUFFER_TIME = 0;

    private static final String DEFAULT_TIMEZONE = "UTC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "doctor_id", nullable = false, unique = true)
    private Doctor doctor;

    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration = DEFAULT_SLOT_DURATION;

    @Column(name = "buffer_time", nullable = false)
    private Integer bufferTime = DEFAULT_BUFFER_TIME;

    @Column(nullable = false)
    private String timezone = DEFAULT_TIMEZONE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DoctorSettings() {}

    public DoctorSettings(
            Doctor doctor,
            Integer slotDuration,
            Integer bufferTime,
            String timezone
    ) {
        this.doctor = doctor;
        this.slotDuration = slotDuration;
        this.bufferTime = bufferTime;
        this.timezone = timezone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Integer getSlotDuration() {
        return slotDuration;
    }

    public void setSlotDuration(Integer slotDuration) {
        this.slotDuration = slotDuration;
    }

    public Integer getBufferTime() {
        return bufferTime;
    }

    public void setBufferTime(Integer bufferTime) {
        this.bufferTime = bufferTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}