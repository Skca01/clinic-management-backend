# Clinic Management System - Backend

A Spring Boot REST API for managing healthcare clinic operations, including appointment scheduling, medical records, and doctor-patient interactions.

## Project Health

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Skca01_clinic-management-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Skca01_clinic-management-backend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Skca01_clinic-management-backend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Skca01_clinic-management-backend)

## Live Demo
* **Frontend Application:** [medicare-beta-orpin.vercel.app](https://medicare-beta-orpin.vercel.app/)
* **API Documentation:** [clinic-management-backend-x4y8.onrender.com/swagger-ui.html](https://clinic-management-backend-x4y8.onrender.com/swagger-ui.html)

## Technologies
* **Language:** Java 17
* **Framework:** Spring Boot 3.2
* **Security:** Spring Security with JWT (Stateless)
* **Persistence:** PostgreSQL (Production), H2 (Testing)
* **Integrations:** Cloudinary (File Uploads), Brevo (Email Service)
* **Analysis:** SonarCloud, JaCoCo

## Core Functionality
* **Authentication:** Secure login and registration for Patients and Doctors.
* **Scheduling:** Real-time appointment booking with doctor availability validation.
* **Medical Records:** Centralized management of diagnoses and patient history.
* **Notifications:** Automated email alerts for appointment status changes.
* **Profile Management:** Dynamic doctor profiles including schedule settings and breaks.

## Development Setup

1. Clone the repository.
2. Create `src/main/resources/application-local.properties` (this file is ignored by Git).
3. Populate the local properties with your environment-specific secrets.
4. Run the application using the `local` profile: `mvn spring-boot:run -Dspring-boot.run.profiles=local`

## API Documentation
The project uses OpenAPI 3.0. When running locally, documentation is available at:
`http://localhost:8080/swagger-ui.html`