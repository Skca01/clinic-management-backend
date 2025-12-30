package com.amante.clinicmanagement.service;

import com.amante.clinicmanagement.dto.request.BookAppointmentRequest;
import com.amante.clinicmanagement.dto.request.RejectAppointmentRequest;
import com.amante.clinicmanagement.dto.response.AppointmentDto;

import java.util.List;

public interface AppointmentService {

    AppointmentDto bookAppointment(
            BookAppointmentRequest request,
            String patientEmail
    );

    AppointmentDto confirmAppointment(
            Long appointmentId,
            String doctorEmail
    );

    AppointmentDto rejectAppointment(
            Long appointmentId,
            String doctorEmail,
            RejectAppointmentRequest request
    );

    AppointmentDto completeAppointment(
            Long appointmentId,
            String doctorEmail
    );

    List<AppointmentDto> getMyAppointments(String userEmail);

    List<AppointmentDto> getDoctorPendingAppointments(String doctorEmail);

    AppointmentDto cancelAppointment(Long appointmentId, String userEmail);

    AppointmentDto getAppointmentById(Long appointmentId);
}