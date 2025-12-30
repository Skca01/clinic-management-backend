package com.amante.clinicmanagement.repository;

import com.amante.clinicmanagement.entity.DoctorWeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorWeeklyScheduleRepository
        extends JpaRepository<DoctorWeeklySchedule, Long> {

    List<DoctorWeeklySchedule> findByDoctorId(Long doctorId);

    Optional<DoctorWeeklySchedule> findByDoctorIdAndDayOfWeek(
            Long doctorId, DayOfWeek dayOfWeek
    );
}