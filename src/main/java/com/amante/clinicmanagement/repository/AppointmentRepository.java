package com.amante.clinicmanagement.repository;

import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.entity.Appointment.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.startTime >= :startOfDay " +
            "AND a.endTime <= :endOfDay " +
            "AND a.status != 'CANCELLED'")
    List<Appointment> findByDoctorIdAndDate(
            @Param("doctorId") Long doctorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND ((a.startTime < :endTime " +
            "AND a.endTime > :startTime)) " +
            "AND a.status != 'CANCELLED'")
    boolean existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, Status status);
}