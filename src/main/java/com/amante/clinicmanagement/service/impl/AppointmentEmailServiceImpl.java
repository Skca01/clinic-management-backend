package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.service.AppointmentEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentEmailServiceImpl implements AppointmentEmailService {

    private static final String EMAIL_KEY = "email";
    private static final String SENDER_KEY = "sender";
    private static final String SUBJECT_KEY = "subject";
    private static final String HTML_CONTENT_KEY = "htmlContent";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h:mm a");

    private final WebClient.Builder webClientBuilder;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.clinic.name}")
    private String clinicName;

    @Value("${email.clinic.address}")
    private String clinicAddress;

    @Value("${email.clinic.city}")
    private String clinicCity;

    @Value("${email.clinic.country}")
    private String clinicCountry;

    @Value("${email.clinic.phone}")
    private String clinicPhone;

    @Value("${email.clinic.website}")
    private String clinicWebsite;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send email notification for pending appointment
     * (to both patient and doctor)
     */
    public void sendPendingAppointmentEmails(Appointment appointment) {
        if (!emailEnabled) {
            log.info(
                    "Email notifications disabled - "
                            + "skipping pending appointment emails"
            );
            return;
        }

        try {
            // Email to patient
            sendPatientPendingEmail(appointment);

            // Email to doctor
            sendDoctorNewRequestEmail(appointment);

            log.info(
                    "✓ Pending appointment emails sent successfully "
                            + "for appointment ID: {}",
                    appointment.getId()
            );
        } catch (Exception e) {
            log.error(
                    "✗ Failed to send pending appointment emails "
                            + "for appointment ID: {}",
                    appointment.getId(),
                    e
            );
            // Don't throw exception - email failure
            // shouldn't stop appointment booking
        }
    }

    /**
     * Send confirmation email to patient when doctor accepts
     */
    public void sendConfirmationEmail(Appointment appointment) {
        if (!emailEnabled) {
            log.info(
                    "Email notifications disabled - skipping confirmation email"
            );
            return;
        }

        try {
            String patientEmail =
                    appointment.getPatient().getUser().getEmail();
            String htmlContent =
                    loadTemplate("templates/patient-confirmed.html");
            htmlContent = replacePlaceholders(htmlContent, appointment);

            Map<String, Object> emailData = Map.of(
                    SENDER_KEY,
                    Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                    "to",
                    List.of(Map.of(EMAIL_KEY, patientEmail)),
                    SUBJECT_KEY,
                    "✓ Appointment Confirmed - " + clinicName,
                    HTML_CONTENT_KEY,
                    htmlContent
            );

            sendEmail(emailData);
            log.info("✓ Confirmation email sent to patient: {}",
                    patientEmail);
        } catch (Exception e) {
            log.error("✗ Failed to send confirmation email", e);
        }
    }

    /**
     * Send rejection email to patient when doctor declines
     */
    public void sendRejectionEmail(Appointment appointment) {
        if (!emailEnabled) {
            log.info(
                    "Email notifications disabled - skipping rejection email"
            );
            return;
        }

        try {
            String patientEmail =
                    appointment.getPatient().getUser().getEmail();
            String htmlContent =
                    loadTemplate("templates/patient-rejected.html");
            htmlContent = replacePlaceholders(htmlContent, appointment);

            Map<String, Object> emailData = Map.of(
                    SENDER_KEY,
                    Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                    "to",
                    List.of(Map.of(EMAIL_KEY, patientEmail)),
                    SUBJECT_KEY,
                    "Appointment Request Update - " + clinicName,
                    HTML_CONTENT_KEY,
                    htmlContent
            );

            sendEmail(emailData);
            log.info("✓ Rejection email sent to patient: {}", patientEmail);
        } catch (Exception e) {
            log.error("✗ Failed to send rejection email", e);
        }
    }

    /**
     * Send completion email to patient
     */
    public void sendCompletionEmail(Appointment appointment) {
        if (!emailEnabled) {
            log.info(
                    "Email notifications disabled - skipping completion email"
            );
            return;
        }

        try {
            String patientEmail =
                    appointment.getPatient().getUser().getEmail();
            String htmlContent =
                    loadTemplate("templates/patient-completed.html");
            htmlContent = replacePlaceholders(htmlContent, appointment);

            Map<String, Object> emailData = Map.of(
                    SENDER_KEY,
                    Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                    "to",
                    List.of(Map.of(EMAIL_KEY, patientEmail)),
                    SUBJECT_KEY,
                    "Thank You for Your Visit - " + clinicName,
                    HTML_CONTENT_KEY,
                    htmlContent
            );

            sendEmail(emailData);
            log.info("✓ Completion email sent to patient: {}", patientEmail);
        } catch (Exception e) {
            log.error("✗ Failed to send completion email", e);
        }
    }

    /**
     * Send cancellation emails to both patient and doctor
     */
    public void sendCancellationEmails(
            Appointment appointment,
            String cancelledBy
    ) {
        if (!emailEnabled) {
            log.info(
                    "Email notifications disabled - " +
                            "skipping cancellation emails"
            );
            return;
        }

        try {
            String patientEmail =
                    appointment.getPatient().getUser().getEmail();
            String doctorEmail =
                    appointment.getDoctor().getUser().getEmail();
            String patientName = appointment.getPatient().getFirstName();
            String doctorName = "Dr. "
                    + appointment.getDoctor().getFirstName()
                    + " "
                    + appointment.getDoctor().getLastName();

            // Load template
            String htmlTemplate =
                    loadTemplate("templates/appointment-cancelled.html");

            // Email to patient
            String patientHtml = buildCancellationEmail(
                    htmlTemplate,
                    appointment,
                    patientName,
                    doctorName,
                    cancelledBy,
                    true
            );

            Map<String, Object> patientEmailData = Map.of(
                    SENDER_KEY,
                    Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                    "to",
                    List.of(Map.of(EMAIL_KEY, patientEmail)),
                    SUBJECT_KEY,
                    "Appointment Cancelled - " + clinicName,
                    HTML_CONTENT_KEY,
                    patientHtml
            );

            sendEmail(patientEmailData);

            // Email to doctor
            String doctorHtml = buildCancellationEmail(
                    htmlTemplate,
                    appointment,
                    patientName,
                    doctorName,
                    cancelledBy,
                    false
            );

            Map<String, Object> doctorEmailData = Map.of(
                    SENDER_KEY,
                    Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                    "to",
                    List.of(Map.of(EMAIL_KEY, doctorEmail)),
                    SUBJECT_KEY,
                    "Appointment Cancelled - " + clinicName,
                    HTML_CONTENT_KEY,
                    doctorHtml
            );

            sendEmail(doctorEmailData);

            log.info(
                    "✓ Cancellation emails sent for appointment ID: {}",
                    appointment.getId()
            );
        } catch (Exception e) {
            log.error("✗ Failed to send cancellation emails", e);
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void sendPatientPendingEmail(Appointment appointment)
            throws IOException {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String htmlContent = loadTemplate("templates/patient-pending.html");
        htmlContent = replacePlaceholders(htmlContent, appointment);

        Map<String, Object> emailData = Map.of(
                SENDER_KEY,
                Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                "to",
                List.of(Map.of(EMAIL_KEY, patientEmail)),
                SUBJECT_KEY,
                "Appointment Request Received - " + clinicName,
                HTML_CONTENT_KEY,
                htmlContent
        );

        sendEmail(emailData);
        log.info("✓ Pending email sent to patient: {}", patientEmail);
    }

    private void sendDoctorNewRequestEmail(Appointment appointment)
            throws IOException {
        String doctorEmail = appointment.getDoctor().getUser().getEmail();
        String patientName = appointment.getPatient().getFirstName()
                + " "
                + appointment.getPatient().getLastName();

        String htmlContent =
                loadTemplate("templates/doctor-new-request.html");
        htmlContent = replacePlaceholders(htmlContent, appointment);

        Map<String, Object> emailData = Map.of(
                SENDER_KEY,
                Map.of(EMAIL_KEY, fromEmail, "name", clinicName),
                "to",
                List.of(Map.of(EMAIL_KEY, doctorEmail)),
                SUBJECT_KEY,
                "🔔 New Appointment Request - " + patientName,
                HTML_CONTENT_KEY,
                htmlContent
        );

        sendEmail(emailData);
        log.info("✓ New request email sent to doctor: {}", doctorEmail);
    }

    private void sendEmail(Map<String, Object> emailData) {
        WebClient webClient = webClientBuilder
                .baseUrl("https://api.brevo.com")
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader("api-key", brevoApiKey)
                .build();

        webClient.post()
                .uri("/v3/smtp/email")
                .bodyValue(emailData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Load HTML template from resources folder
     */
    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    /**
     * Replace placeholders in HTML template with actual values
     * FIXED: Now properly separates doctor's clinic address (body)
     * from company address (footer)
     */
    private String replacePlaceholders(
            String template,
            Appointment appointment
    ) {
        String patientName = appointment.getPatient().getFirstName();
        String patientFullName =
                appointment.getPatient().getFirstName()
                        + " "
                        + appointment.getPatient().getLastName();
        String doctorName = "Dr. "
                + appointment.getDoctor().getFirstName()
                + " "
                + appointment.getDoctor().getLastName();
        String doctorFirstName =
                appointment.getDoctor().getFirstName()
                        + " "
                        + appointment.getDoctor().getLastName();
        String appointmentDate =
                appointment.getStartTime().format(DATE_FORMATTER);
        String appointmentTime =
                appointment.getStartTime().format(TIME_FORMATTER);
        String appointmentEndTime =
                appointment.getEndTime().format(TIME_FORMATTER);

        String patientNotes;
        if (appointment.getPatientNotes() != null) {
            patientNotes = appointment.getPatientNotes();
        } else {
            patientNotes = "No additional notes provided";
        }

        String rejectionReason;
        if (appointment.getRejectionReason() != null) {
            rejectionReason = appointment.getRejectionReason();
        } else {
            rejectionReason = "The requested time slot is not available";
        }

        // Get doctor's clinic address (for appointment location in email body)
        String doctorClinicAddress;
        if (appointment.getDoctor().getClinicAddress() != null) {
            doctorClinicAddress = appointment.getDoctor().getClinicAddress();
        } else {
            doctorClinicAddress = "Clinic address not provided";
        }

        String doctorClinicCity;
        if (appointment.getDoctor().getClinicCity() != null) {
            doctorClinicCity = appointment.getDoctor().getClinicCity();
        } else {
            doctorClinicCity = "City";
        }

        String doctorClinicCountry;
        if (appointment.getDoctor().getClinicCountry() != null) {
            doctorClinicCountry = appointment.getDoctor().getClinicCountry();
        } else {
            doctorClinicCountry = "Country";
        }

        return template
                // Company/System info (for header and footer)
                .replace("{clinicName}", clinicName)
                // Footer company address
                .replace("{companyAddress}", clinicAddress)
                // Footer company city
                .replace("{companyCity}", clinicCity)
                // Footer company country
                .replace("{companyCountry}", clinicCountry)
                .replace("{clinicPhone}", clinicPhone)
                .replace("{websiteUrl}", clinicWebsite)
                // Doctor's clinic address (for appointment location in body)
                .replace("{clinicAddress}", doctorClinicAddress)
                .replace("{clinicCity}", doctorClinicCity)
                .replace("{clinicCountry}", doctorClinicCountry)
                // Patient info
                .replace("{patientName}", patientName)
                .replace("{patientFullName}", patientFullName)
                .replace("{patientPhone}", appointment.getPatient().getPhone())
                .replace(
                        "{patientGender}",
                        appointment.getPatient().getGender()
                )
                // Doctor info
                .replace("{doctorName}", doctorName)
                .replace("{doctorFirstName}", doctorFirstName)
                .replace(
                        "{doctorSpecialization}",
                        appointment.getDoctor().getSpecialization()
                )
                // Appointment details
                .replace("{appointmentDate}", appointmentDate)
                .replace("{appointmentTime}", appointmentTime)
                .replace("{appointmentEndTime}", appointmentEndTime)
                .replace("{patientNotes}", patientNotes)
                .replace("{rejectionReason}", rejectionReason);
    }

    /**
     * Build cancellation email with recipient-specific content
     */
    private String buildCancellationEmail(
            String template,
            Appointment appointment,
            String patientName,
            String doctorName,
            String cancelledBy,
            boolean isPatient
    ) {
        String recipientName;
        if (isPatient) {
            recipientName = patientName;
        } else {
            recipientName = "Dr. " + doctorName.replace("Dr. ", "");
        }

        String cancellerName;
        if (cancelledBy.equals("PATIENT")) {
            cancellerName = patientName;
        } else {
            cancellerName = doctorName;
        }

        String otherPartyLabel;
        if (isPatient) {
            otherPartyLabel = "Doctor";
        } else {
            otherPartyLabel = "Patient";
        }

        String otherPartyName;
        if (isPatient) {
            otherPartyName = doctorName;
        } else {
            otherPartyName = patientName;
        }

        String ctaButtonText;
        if (isPatient) {
            ctaButtonText = "Book New Appointment";
        } else {
            ctaButtonText = "View My Schedule";
        }

        String additionalMessage;
        if (isPatient) {
            additionalMessage = "<div style=\"background-color: #f9fafb; "
                    + "border-radius: 8px; padding: 20px; margin: 25px 0;\">"
                    + "<h3 style=\"margin: 0 0 12px 0; color: #1f2937; "
                    + "font-size: 16px; font-weight: 600;\">"
                    + "Need to reschedule?</h3>"
                    + "<p style=\"margin: 0; color: #4b5563; "
                    + "font-size: 14px; line-height: 1.6;\">"
                    + "We understand that plans change. "
                    + "You can book a new appointment with "
                    + doctorName
                    + " or browse other available doctors."
                    + "</p>"
                    + "</div>";
        } else {
            additionalMessage = "<div style=\"background-color: #f9fafb; "
                    + "border-radius: 8px; padding: 20px; margin: 25px 0;\">"
                    + "<p style=\"margin: 0; color: #4b5563; "
                    + "font-size: 14px; line-height: 1.6;\">"
                    + "Your schedule has been updated to reflect "
                    + "this cancellation."
                    + "</p>"
                    + "</div>";
        }

        String appointmentDate =
                appointment.getStartTime().format(DATE_FORMATTER);
        String appointmentTime =
                appointment.getStartTime().format(TIME_FORMATTER);

        return template
                .replace("{clinicName}", clinicName)
                // Footer uses company address
                .replace("{companyAddress}", clinicAddress)
                .replace("{clinicPhone}", clinicPhone)
                .replace("{websiteUrl}", clinicWebsite)
                .replace("{recipientName}", recipientName)
                .replace("{cancellerName}", cancellerName)
                .replace("{appointmentDate}", appointmentDate)
                .replace("{appointmentTime}", appointmentTime)
                .replace("{otherPartyLabel}", otherPartyLabel)
                .replace("{otherPartyName}", otherPartyName)
                .replace("{additionalMessage}", additionalMessage)
                .replace("{ctaButtonText}", ctaButtonText);
    }
}