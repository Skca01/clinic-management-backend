package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.entity.Appointment;
import com.amante.clinicmanagement.entity.Doctor;
import com.amante.clinicmanagement.entity.Patient;
import com.amante.clinicmanagement.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentEmailServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AppointmentEmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> emailDataCaptor;

    private MockedConstruction<ClassPathResource> mockedResource;

    private Appointment appointment;
    private Patient patient;
    private Doctor doctor;
    private User patientUser;
    private User doctorUser;

    private final String testTemplate = """
            <html>
            <body>
                {clinicName}
                {companyAddress}
                {companyCity}
                {companyCountry}
                {clinicPhone}
                {websiteUrl}
                {clinicAddress}
                {clinicCity}
                {clinicCountry}
                {patientName}
                {patientFullName}
                {patientPhone}
                {patientGender}
                {doctorName}
                {doctorFirstName}
                {doctorSpecialization}
                {appointmentDate}
                {appointmentTime}
                {appointmentEndTime}
                {patientNotes}
                {rejectionReason}
                {recipientName}
                {cancellerName}
                {otherPartyLabel}
                {otherPartyName}
                {additionalMessage}
                {ctaButtonText}
            </body>
            </html>
            """;

    @BeforeEach
    void setUp() {
        // Setup WebClient mocks
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);

        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).bodyValue(any());
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("success"));

        // Set up configuration properties
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@clinic.com");
        ReflectionTestUtils.setField(emailService, "clinicName", "Test Clinic");
        ReflectionTestUtils.setField(emailService, "clinicAddress", "123 Main St");
        ReflectionTestUtils.setField(emailService, "clinicCity", "Test City");
        ReflectionTestUtils.setField(emailService, "clinicCountry", "Test Country");
        ReflectionTestUtils.setField(emailService, "clinicPhone", "+1234567890");
        ReflectionTestUtils.setField(emailService, "clinicWebsite", "https://clinic.com");
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);

        // Create test entities
        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setEmail("patient@test.com");

        doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setEmail("doctor@test.com");

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);
        patient.setFirstName("Kent");
        patient.setLastName("Carlo");
        patient.setPhone("+1234567890");
        patient.setGender("Male");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(doctorUser);
        doctor.setFirstName("Jane");
        doctor.setLastName("Smith");
        doctor.setSpecialization("Cardiologist");
        doctor.setClinicAddress("456 Medical Plaza");
        doctor.setClinicCity("Medical City");
        doctor.setClinicCountry("Medical Country");

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(LocalDateTime.of(2024, 12, 25, 10, 0));
        appointment.setEndTime(LocalDateTime.of(2024, 12, 25, 11, 0));
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setPatientNotes("Test notes");
        appointment.setRejectionReason("Test rejection reason");

        // Mock ClassPathResource
        mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(testTemplate.getBytes()));
                    when(mock.exists()).thenReturn(true);
                });
    }

    @AfterEach
    void tearDown() {
        if (mockedResource != null) {
            mockedResource.close();
        }
    }

    // ==================== PENDING APPOINTMENT EMAILS TESTS ====================

    @Test
    void sendPendingAppointmentEmails_SuccessAndEmailDisabledAndError() throws IOException {
        // Test 1: Success
        emailService.sendPendingAppointmentEmails(appointment);

        verify(webClient, times(2)).post();
        verify(requestBodyUriSpec, times(2)).bodyValue(emailDataCaptor.capture());

        List<Map<String, Object>> capturedEmails = emailDataCaptor.getAllValues();
        assertThat(capturedEmails.get(0).get("subject")).isEqualTo("Appointment Request Received - Test Clinic");
        assertThat(capturedEmails.get(1).get("subject")).asString().contains("New Appointment Request");

        // Test 2: Email disabled
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        emailService.sendPendingAppointmentEmails(appointment);
        verify(webClient, times(2)).post(); // Still 2 from test 1, no new calls

        // Test 3: Exception handled
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        assertDoesNotThrow(() -> emailService.sendPendingAppointmentEmails(appointment));
        verify(webClient, atLeast(3)).post(); // At least 3 total calls now
    }

    // ==================== CONFIRMATION EMAIL TESTS ====================

    @Test
    void sendConfirmationEmail_AllScenarios() throws IOException {
        // Test 1: Success
        emailService.sendConfirmationEmail(appointment);
        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        assertThat(emailDataCaptor.getValue().get("subject")).asString().contains("Appointment Confirmed");

        // Test 2: Email disabled
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        emailService.sendConfirmationEmail(appointment);
        verify(webClient, times(1)).post(); // No new call

        // Test 3: Exception handled
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        assertDoesNotThrow(() -> emailService.sendConfirmationEmail(appointment));
        verify(webClient, times(2)).post();
    }

    // ==================== REJECTION EMAIL TESTS ====================

    @Test
    void sendRejectionEmail_AllScenarios() throws IOException {
        // Test 1: Success
        emailService.sendRejectionEmail(appointment);
        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        assertThat(emailDataCaptor.getValue().get("subject")).asString().contains("Appointment Request Update");

        // Test 2: Email disabled
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        emailService.sendRejectionEmail(appointment);
        verify(webClient, times(1)).post();

        // Test 3: Exception handled
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        assertDoesNotThrow(() -> emailService.sendRejectionEmail(appointment));
        verify(webClient, times(2)).post();
    }

    // ==================== COMPLETION EMAIL TESTS ====================

    @Test
    void sendCompletionEmail_AllScenarios() throws IOException {
        // Test 1: Success
        emailService.sendCompletionEmail(appointment);
        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        assertThat(emailDataCaptor.getValue().get("subject")).asString().contains("Thank You for Your Visit");

        // Test 2: Email disabled
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        emailService.sendCompletionEmail(appointment);
        verify(webClient, times(1)).post();

        // Test 3: Exception handled
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        assertDoesNotThrow(() -> emailService.sendCompletionEmail(appointment));
        verify(webClient, times(2)).post();
    }

    // ==================== CANCELLATION EMAIL TESTS ====================

    @Test
    void sendCancellationEmails_AllScenarios() throws IOException {
        // Test 1: Cancelled by patient
        emailService.sendCancellationEmails(appointment, "PATIENT");
        verify(webClient, times(2)).post();
        verify(requestBodyUriSpec, times(2)).bodyValue(emailDataCaptor.capture());
        emailDataCaptor.getAllValues().forEach(email ->
                assertThat(email.get("subject")).asString().contains("Appointment Cancelled")
        );

        // Test 2: Cancelled by doctor
        emailService.sendCancellationEmails(appointment, "DOCTOR");
        verify(webClient, times(4)).post(); // 2 + 2 = 4 total

        // Test 3: Email disabled
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        emailService.sendCancellationEmails(appointment, "PATIENT");
        verify(webClient, times(4)).post(); // No new call

        // Test 4: Exception handled
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        assertDoesNotThrow(() -> emailService.sendCancellationEmails(appointment, "PATIENT"));
        verify(webClient, atLeast(5)).post();
    }

    // ==================== PLACEHOLDER REPLACEMENT TESTS ====================

    @Test
    void replacePlaceholders_NullAndDefaultValues() throws IOException {
        // Test 1: Null patient notes
        appointment.setPatientNotes(null);
        emailService.sendConfirmationEmail(appointment);
        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        String htmlContent = (String) emailDataCaptor.getValue().get("htmlContent");
        assertThat(htmlContent).contains("No additional notes provided");

        // Test 2: Null rejection reason - create a fresh appointment to ensure null
        Appointment appointmentWithoutRejection = new Appointment();
        appointmentWithoutRejection.setId(1L);
        appointmentWithoutRejection.setPatient(patient);
        appointmentWithoutRejection.setDoctor(doctor);
        appointmentWithoutRejection.setStartTime(LocalDateTime.of(2024, 12, 25, 10, 0));
        appointmentWithoutRejection.setEndTime(LocalDateTime.of(2024, 12, 25, 11, 0));
        appointmentWithoutRejection.setStatus(Appointment.Status.PENDING);
        appointmentWithoutRejection.setPatientNotes("Test notes");
        appointmentWithoutRejection.setRejectionReason(null); // Explicitly null

        emailService.sendRejectionEmail(appointmentWithoutRejection);
        verify(requestBodyUriSpec, times(2)).bodyValue(emailDataCaptor.capture());

        // Find the rejection email by checking the subject
        List<Map<String, Object>> allCaptured = emailDataCaptor.getAllValues();
        Map<String, Object> rejectionEmail = allCaptured.stream()
                .filter(email -> email.get("subject").toString().contains("Appointment Request Update"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Rejection email not found"));

        htmlContent = (String) rejectionEmail.get("htmlContent");
        assertThat(htmlContent).contains("The requested time slot is not available");

        // Test 3: Null doctor clinic info - modify doctor BEFORE sending
        doctor.setClinicAddress(null);
        doctor.setClinicCity(null);
        doctor.setClinicCountry(null);

        // Reset appointment for clean test
        appointment.setPatientNotes("Test notes");

        emailService.sendConfirmationEmail(appointment);
        verify(requestBodyUriSpec, times(3)).bodyValue(emailDataCaptor.capture());

        // Get all captured emails and find the last one (third confirmation email)
        allCaptured = emailDataCaptor.getAllValues();
        // The last captured email should be the one we just sent
        Map<String, Object> lastEmail = allCaptured.get(allCaptured.size() - 1);
        htmlContent = (String) lastEmail.get("htmlContent");

        assertThat(htmlContent).contains("Clinic address not provided");
        assertThat(htmlContent).contains("City");
        assertThat(htmlContent).contains("Country");
    }

    @Test
    void replacePlaceholders_AllFieldsPopulated() throws IOException {
        emailService.sendConfirmationEmail(appointment);

        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        String htmlContent = (String) emailDataCaptor.getValue().get("htmlContent");

        // Verify all major placeholders replaced
        assertThat(htmlContent)
                .contains("Test Clinic", "123 Main St", "Test City", "Test Country",
                        "+1234567890", "https://clinic.com", "Kent", "Kent Carlo",
                        "Dr. Jane Smith", "Cardiologist", "456 Medical Plaza",
                        "Medical City", "Medical Country", "Male", "Test notes");
    }

    // ==================== CANCELLATION EMAIL CONTENT TESTS ====================

    @Test
    void buildCancellationEmail_PatientAndDoctorRecipients() throws IOException {
        emailService.sendCancellationEmails(appointment, "PATIENT");

        verify(requestBodyUriSpec, times(2)).bodyValue(emailDataCaptor.capture());
        List<Map<String, Object>> emails = emailDataCaptor.getAllValues();

        // Patient email
        String patientEmail = (String) emails.get(0).get("htmlContent");
        assertThat(patientEmail).contains("Book New Appointment", "Need to reschedule?");

        // Doctor email
        String doctorEmail = (String) emails.get(1).get("htmlContent");
        assertThat(doctorEmail).contains("View My Schedule", "Your schedule has been updated");
    }

    // ==================== EMAIL STRUCTURE AND FORMATTING TESTS ====================

    @Test
    void emailDataStructure_HasCorrectFormat() throws IOException {
        emailService.sendConfirmationEmail(appointment);

        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        Map<String, Object> emailData = emailDataCaptor.getValue();

        // Verify structure
        assertThat(emailData).containsKeys("sender", "to", "subject", "htmlContent");

        @SuppressWarnings("unchecked")
        Map<String, Object> sender = (Map<String, Object>) emailData.get("sender");
        assertThat(sender.get("email")).isEqualTo("noreply@clinic.com");
        assertThat(sender.get("name")).isEqualTo("Test Clinic");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recipients = (List<Map<String, Object>>) emailData.get("to");
        assertThat(recipients).hasSize(1);
        assertThat(recipients.get(0).get("email")).isEqualTo("patient@test.com");
    }

    @Test
    void dateTimeFormatting_CorrectFormat() throws IOException {
        emailService.sendConfirmationEmail(appointment);

        verify(requestBodyUriSpec).bodyValue(emailDataCaptor.capture());
        String htmlContent = (String) emailDataCaptor.getValue().get("htmlContent");

        // Verify date and time formatting (case-insensitive for AM/PM to support different locales)
        assertThat(htmlContent)
                .contains("Wednesday, December 25, 2024");

        // Check time format with case-insensitive comparison (Windows uses "am/pm", Linux uses "AM/PM")
        assertThat(htmlContent.toLowerCase())
                .contains("10:00 am")
                .contains("11:00 am");
    }

    @Test
    void webClientConfiguration_CorrectSetup() throws IOException {
        emailService.sendConfirmationEmail(appointment);

        verify(webClientBuilder).baseUrl("https://api.brevo.com");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("api-key", "test-api-key");
        verify(requestBodyUriSpec).uri("/v3/smtp/email");
    }
}