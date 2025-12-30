package com.amante.clinicmanagement.repository;

import com.amante.clinicmanagement.entity.DoctorBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorBreakRepository
        extends JpaRepository<DoctorBreak, Long> {

    List<DoctorBreak> findByDoctorId(Long doctorId);

    @Query("SELECT b FROM DoctorBreak b " +
            "WHERE b.doctor.id = :doctorId " +
            "AND (b.dayOfWeek = 'ALL' OR b.dayOfWeek = :dayOfWeek)")
    List<DoctorBreak> findByDoctorIdAndDay(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") String dayOfWeek
    );
}