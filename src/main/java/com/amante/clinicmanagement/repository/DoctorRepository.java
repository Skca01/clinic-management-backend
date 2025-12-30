package com.amante.clinicmanagement.repository;

import com.amante.clinicmanagement.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findBySpecializationContainingIgnoreCase(
            String specialization);

    List<Doctor> findByClinicCountryContainingIgnoreCase(String country);

    List<Doctor> findByClinicCityContainingIgnoreCase(String city);

    List<Doctor>
    findByClinicCountryContainingIgnoreCaseAndClinicCityContainingIgnoreCase(
            String country,
            String city
    );

    @Query("SELECT d FROM Doctor d WHERE " +
            "(:country IS NULL OR " +
            "LOWER(d.clinicCountry) LIKE LOWER(CONCAT('%', :country, '%'))) " +
            "AND (:city IS NULL OR " +
            "LOWER(d.clinicCity) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:specialization IS NULL OR " +
            "LOWER(d.specialization) LIKE " +
            "LOWER(CONCAT('%', :specialization, '%')))")
    List<Doctor> findByLocationAndSpecialization(
            @Param("country") String country,
            @Param("city") String city,
            @Param("specialization") String specialization
    );
}