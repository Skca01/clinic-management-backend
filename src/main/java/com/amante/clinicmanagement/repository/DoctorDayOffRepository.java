package com.amante.clinicmanagement.repository;

import com.amante.clinicmanagement.entity.DoctorDayOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorDayOffRepository
        extends JpaRepository<DoctorDayOff, Long> {

    List<DoctorDayOff> findByDoctorId(Long doctorId);

    @Query(
            "SELECT d FROM DoctorDayOff d " +
                    "WHERE d.doctor.id = :doctorId " +
                    "AND d.isRecurring = false " +
                    "AND :date BETWEEN d.startDate AND d.endDate"
    )
    List<DoctorDayOff> findByDoctorIdAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date
    );

    // Find recurring days off for a specific day of week
    @Query(
            "SELECT d FROM DoctorDayOff d " +
                    "WHERE d.doctor.id = :doctorId " +
                    "AND d.isRecurring = true " +
                    "AND d.recurringDay = :dayOfWeek"
    )
    List<DoctorDayOff> findRecurringByDoctorIdAndDayOfWeek(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
}