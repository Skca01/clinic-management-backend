package com.amante.clinicmanagement.service;

import com.amante.clinicmanagement.entity.Appointment;

public interface AppointmentEmailService {
    void sendPendingAppointmentEmails(Appointment appointment);

    void sendConfirmationEmail(Appointment appointment);

    void sendRejectionEmail(Appointment appointment);

    void sendCompletionEmail(Appointment appointment);

    void sendCancellationEmails(Appointment appointment, String cancelledBy);
}