# Digital Clinic Appointment & Booking System (HealthConnect)

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Security-JWT%20HS256-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

A full-stack, enterprise-grade Digital Clinic Appointment and Healthcare Management System designed to streamline outpatient clinic operations, patient consultations, doctor scheduling, electronic medical records (EMR), digital prescriptions, and administrative workflows.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Project Architecture & Structure](#project-architecture--structure)
- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [MySQL Database Setup](#mysql-database-setup)
- [Running the Application](#running-the-application)
- [Frontend Navigation & Portals](#frontend-navigation--portals)
- [User Roles & Registration Flow](#user-roles--registration-flow)
- [API Endpoints Reference](#api-endpoints-reference)
- [Important Setup Notes & Best Practices](#important-setup-notes--best-practices)

---

## Overview

**HealthConnect** connects patients, healthcare providers, and clinic administrators through an integrated web platform. Patients can search for qualified doctors, view clinic availability, schedule in-person or video consultations, book emergency slots, track medical histories, and download prescriptions. Doctors can manage schedules, inspect patient dossiers, record vitals, and generate formatted PDF prescriptions. Administrators maintain full visibility through an analytics dashboard, manage doctor verification, and configure clinic-wide operational rules.

---

## Key Features

### 👤 Patient Management
- **Self-Service Registration & Authentication:** Secure registration and JWT-based login with role-based authorization.
- **Doctor Discovery:** Filter doctors by specialty, consultation fee, clinic location, or ratings.
- **Appointment Booking:** Real-time scheduling for standard appointments and triage-tagged emergency slots.
- **Digital Passes:** Automated booking reference generation with QR code check-in passes.
- **Health Records:** Secure storage for uploaded lab results, historical vitals charts, and past consultation notes.

### 👨‍⚕️ Doctor Workspace
- **Dynamic Scheduling:** Configure weekly availability, time windows, and slot durations (e.g. 15, 30, or 60 minutes) across multiple clinic locations.
- **Patient History & Vitals:** Record and track longitudinal patient vitals (Blood Pressure, Heart Rate, SpO2, Temperature, BMI, Blood Sugar).
- **Prescription Generator:** Structured prescription creation with medication dosage, timing, frequency, instructions, and automatic PDF rendering via iText.

### 🏥 Hospital & Clinic Network
- **Multi-Location Support:** Link doctors to multiple clinic branches and affiliated hospitals.
- **Hospital Directory:** Discover accredited hospitals with emergency services, operating hours, and contact details.

### 🔒 Administration & Analytics
- **Doctor Verification Workflow:** Admin oversight to approve and verify newly registered medical practitioners.
- **System Settings:** Configurable operational parameters (slot durations, maximum daily consultations, fee currency, emergency booking toggles).
- **Clinic Analytics:** High-level metrics, revenue tracking, consultation volume trends, and CSV reporting export.

### ⚡ Real-Time Capabilities & Payments
- **WebSocket Notifications:** STOMP-over-WebSocket notifications for appointment updates, status changes, and reminders.
- **Simulated Payment Workflow:** Mock payment gateway integration for appointment consultation fees.

---

## Technologies Used

| Domain | Technology | Description |
|---|---|---|
| **Backend Framework** | Spring Boot 3.2.3 | Core RESTful API and microservice backend |
| **Language** | Java 17 / 21 | Modern LTS Java runtime support |
| **Persistence** | Spring Data JPA / Hibernate 6 | Relational data mapping and query execution |
| **Database** | MySQL 8.x | Primary relational database engine |
| **Security** | Spring Security 6 & JJWT (0.11.5) | Stateless JWT authentication, BCrypt password hashing |
| **Real-time** | Spring WebSocket & STOMP | Real-time user push notifications |
| **Document Generation** | iTextPDF 5.5.13 | Server-side PDF prescription generation |
| **QR Code** | ZXing 3.5.1 | Appointment QR code generation |
| **DTO Mapping** | ModelMapper 3.1.1 | Object model transformations |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+) | Clean, responsive UI with dedicated CSS and modular JS |
| **UI Components** | FontAwesome & Chart.js | Iconography and health vitals charting |
| **Build & Tooling** | Maven 3.8+ / Maven Wrapper | Dependency management and build automation |

---

## Project Architecture & Structure

```
final_healthconnect/
├── .mvn/                                # Maven wrapper configuration
│   └── wrapper/maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/digitalclinic/appointmentsystem/
│   │   │   ├── config/                  # Security, WebMvc, WebSocket, ModelMapper configs
│   │   │   ├── controller/              # REST API controllers
│   │   │   ├── dto/                     # Request and Response Data Transfer Objects
│   │   │   ├── model/                   # JPA Entity models (19 relational entities)
│   │   │   ├── repository/              # Spring Data JPA repositories
│   │   │   ├── security/                # JWT filters, token provider, UserDetailsService
│   │   │   ├── service/                 # Core business logic services
│   │   │   └── AppointmentSystemApplication.java
│   │   └── resources/
│   │       ├── application.yml          # Core Spring Boot application properties
│   │       ├── schema.sql               # Database initialization DDL & default system settings
│   │       └── static/                  # Frontend HTML, modular CSS, and JavaScript
│   │           ├── css/                 # 24 page-specific CSS stylesheets
│   │           ├── js/                  # 27 page-specific JavaScript modules
│   │           └── *.html               # 26 frontend portal HTML pages
│   └── test/                            # Unit and integration test suites
├── uploads/                             # Upload directories (.gitkeep preserved, contents ignored)
│   ├── doctors/
│   ├── prescriptions/
│   └── records/
├── .env.example                         # Template for local environment variables
├── application-local.properties.example # Alternative Spring properties template
├── .gitignore                           # Comprehensive Git exclusion rules
├── pom.xml                              # Maven project definition and dependencies
├── mvnw / mvnw.cmd                      # Cross-platform Maven wrapper scripts
├── sanitized_schema.sql                 # Clean initial database schema
├── CONTRIBUTING.md                      # Contribution guidelines
└── README.md                            # Main project documentation
```

---

## Prerequisites

Before running the application, ensure the following are installed:

- **Java Development Kit (JDK):** Version 17 or 21 (`java -version`)
- **MySQL Server:** Version 8.0 or higher (`mysql --version`)
- **Maven:** Version 3.8+ (optional; the repository includes `./mvnw` and `mvnw.cmd`)
- **Modern Web Browser:** Chrome, Firefox, Edge, or Safari

---

## Environment Configuration

HealthConnect uses environment variables to prevent credentials and secrets from being committed to version control.

### Step 1: Create Your Local `.env` File

Copy the provided `.env.example` to `.env` in the project root:

```bash
# On Linux / macOS
cp .env.example .env

# On Windows (PowerShell)
Copy-Item .env.example .env
```

### Step 2: Configure Environment Variables

Edit `.env` with your local database and environment settings:

```properties
# Database Connection
DB_HOST=localhost
DB_PORT=3306
DB_NAME=healthconnect
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

# Application Server
SERVER_PORT=8080

# JWT Configuration (Required: Minimum 256 bits / 32+ characters for HS256)
JWT_SECRET=your_secure_random_jwt_secret_must_be_at_least_256_bits_long
JWT_EXPIRATION=86400000

# SQL Initialization Mode
# Set to 'never' for normal operation (recommended), or 'always' for fresh initialization
SQL_INIT_MODE=never

# CORS & Upload Directories
ALLOWED_ORIGINS=http://localhost:8080,http://localhost:3000
UPLOAD_DIR=uploads/records
UPLOAD_PRESCRIPTIONS_DIR=uploads/prescriptions
```

> **Note:** `.env` and `application-local.properties` are listed in `.gitignore` and will never be committed to Git.

---

## MySQL Database Setup

1. Start your local MySQL service:
   ```bash
   # Linux / macOS
   sudo systemctl start mysql

   # Windows (PowerShell as Admin)
   Start-Service -Name MySQL80
   ```

2. Open your MySQL client (CLI, MySQL Workbench, or DBeaver) and create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS healthconnect;
   ```

3. Initialize the schema:
   Execute `sanitized_schema.sql` (or `src/main/resources/schema.sql`) to create all 19 tables, foreign keys, indexes, and default system settings.

---

## Running the Application

### Option A: Using the Maven Wrapper (Recommended)

**On Windows (PowerShell / Command Prompt):**
```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
./mvnw clean compile
./mvnw spring-boot:run
```

### Option B: Building and Running the Packaged JAR

```bash
# Build JAR without running database-dependent unit tests
# On Linux / macOS:
./mvnw clean package -DskipTests

# On Windows:
.\mvnw.cmd clean package -DskipTests

# Run the compiled JAR
java -jar target/healthconnect-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`.

---

## Frontend Navigation & Portals

Once the server is running, access the portal at:
```
http://localhost:8080/index.html
```

### Direct Page Routes

| Page | URL Path | Description |
|---|---|---|
| **Home / Landing** | `/index.html` or `/` | Clinic introduction, services, and doctor highlights |
| **Login** | `/login.html` or `/login` | Unified JWT authentication portal |
| **Patient Registration** | `/register_patient.html` or `/register-patient` | New patient account registration |
| **Doctor Registration** | `/register_doctor.html` or `/register-doctor` | Doctor registration & credential submission |
| **Patient Dashboard** | `/patient_dashboard.html` | Patient overview, upcoming appointments, and quick actions |
| **Appointment Booking** | `/appointment_booking.html` | Interactive slot selection and booking flow |
| **Emergency Booking** | `/emergency_appointment.html` | Fast-track urgent consultation booking |
| **Appointment History** | `/appointment_list.html` | Chronological list of user appointments |
| **Appointment Details** | `/appointment_details.html` | Detailed view with QR code pass and cancellation |
| **Medical Records** | `/medical_records.html` | Patient health record dossier and file uploads |
| **Prescription View** | `/prescription_view.html` | View prescription details and download generated PDF |
| **Doctor Search** | `/doctor_search.html` | Search and filter verified doctors |
| **Doctor Dashboard** | `/doctor_dashboard.html` | Daily schedule, consultation queue, and patient stats |
| **Doctor Patients** | `/doctor_patients.html` | List of patients consulted by the logged-in doctor |
| **Patient History (Doctor)** | `/doctor_patient_history.html` | Longitudinal patient records, vitals, and notes |
| **Doctor Schedule** | `/doctor_schedule.html` | Availability and slot duration management |
| **Admin Dashboard** | `/admin_dashboard.html` or `/admin` | System overview, live KPIs, and quick administration |
| **Admin Users** | `/admin_users.html` | User account management and doctor verification |
| **Admin Reports** | `/admin_reports.html` | Statistical trends, appointment volumes, and CSV export |
| **Admin Settings** | `/admin_settings.html` | Dynamic system settings and operational rules |
| **Checkout** | `/checkout.html` | Consultation fee payment flow |

---

## User Roles & Registration Flow

1. **Patients:**
   - Register directly via `/register_patient.html` (or `POST /api/auth/register`).
   - Log in via `/login.html` to access the Patient Dashboard, search doctors, book appointments, and view medical records.

2. **Doctors:**
   - Submit registration applications via `/register_doctor.html` (or `POST /api/doctors/register`).
   - Doctors require administrator verification (`is_verified = TRUE`) before appearing in public searches and taking patient appointments.

3. **Administrators:**
   - Manage clinic operations, verify newly registered doctors, inspect audit logs, and configure system rules via `/admin_dashboard.html`.

---

## API Endpoints Reference

All API endpoints under `/api` (except public authentication and doctor search endpoints) require a valid JWT token sent in the `Authorization` header:
```
Authorization: Bearer <your_jwt_token>
```

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new patient account |
| `POST` | `/api/auth/login` | Public | Authenticate user credentials and receive JWT |
| `GET` | `/api/auth/profile` | Authenticated | Retrieve current authenticated user profile |
| `GET` | `/api/auth/test-hash` | Public | Helper endpoint for password verification |

### 2. Appointments (`/api/appointments`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/appointments` | Patient | Book a standard consultation |
| `POST` | `/api/appointments/emergency` | Patient | Book an emergency appointment |
| `GET` | `/api/appointments/patient` | Patient | Get all appointments for current patient |
| `GET` | `/api/appointments/doctor` | Doctor | Get all appointments for current doctor |
| `GET` | `/api/appointments/{id}` | Authenticated | Retrieve appointment details by ID |
| `GET` | `/api/appointments/{id}/qrcode` | Authenticated | Stream appointment QR code PNG image |
| `PUT` | `/api/appointments/{id}/status` | Doctor / Admin | Update appointment status (CONFIRMED, COMPLETED, etc.) |
| `PUT` | `/api/appointments/{id}/notes` | Doctor | Save consultation clinical notes |
| `DELETE` | `/api/appointments/{id}` | Authenticated | Cancel appointment with cancellation reason |

### 3. Doctors (`/api/doctors`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/doctors` | Public | List all doctors |
| `GET` | `/api/doctors/{id}` | Public | Get doctor profile and clinic affiliations |
| `GET` | `/api/doctors/search` | Public | Search doctors by query or location |
| `GET` | `/api/doctors/specialization/{spec}` | Public | Filter doctors by specialization |
| `GET` | `/api/doctors/top-rated` | Public | Retrieve top-rated practitioners |
| `GET` | `/api/doctors/{id}/availability` | Public | Check available booking slots for a date |
| `GET` | `/api/doctors/{id}/schedule` | Authenticated | Retrieve configured weekly schedule |
| `PUT` | `/api/doctors/{id}/schedule` | Doctor / Admin | Update doctor consultation schedule |
| `POST` | `/api/doctors/register` | Public | Submit doctor application and credentials |
| `GET` | `/api/doctors/{id}/patients` | Doctor | List patients assigned to doctor |
| `GET` | `/api/doctors/{id}/patients/{patientId}/history` | Doctor | Get patient consultation and vitals history |

### 4. Patients & Vitals (`/api/patients`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/patients/profile` | Patient | Retrieve patient profile details |
| `PUT` | `/api/patients/profile` | Patient | Update address, contact, and demographics |
| `GET` | `/api/patients/medical-history` | Patient | Retrieve stored allergies and medical history |
| `PUT` | `/api/patients/medical-history` | Patient | Update medical history details |
| `POST` | `/api/patients/{id}/vitals` | Doctor | Record vitals for a patient consultation |
| `GET` | `/api/patients/{id}/vitals` | Authenticated | Retrieve historical vitals timeline |

### 5. Prescriptions (`/api/prescriptions`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/prescriptions` | Doctor | Create prescription with medication items |
| `GET` | `/api/prescriptions/{id}` | Authenticated | Get prescription by ID |
| `GET` | `/api/prescriptions/patient/{patientId}` | Authenticated | List all prescriptions for a patient |
| `GET` | `/api/prescriptions/download/{fileName}` | Authenticated | Download prescription PDF document |

### 6. Medical Records (`/api/records`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/records/upload` | Authenticated | Upload medical document (multipart form) |
| `GET` | `/api/records/patient/{patientId}` | Authenticated | Get list of uploaded medical records |
| `GET` | `/api/records/{id}` | Authenticated | Get medical record metadata |
| `GET` | `/api/records/download/{fileName}` | Authenticated | Download binary record document |

### 7. Hospitals & Clinics (`/api/hospitals`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/hospitals` | Public | List verified hospitals |
| `GET` | `/api/hospitals/{id}` | Public | Get hospital profile and facilities |
| `GET` | `/api/hospitals/search` | Public | Search hospitals by query |
| `GET` | `/api/hospitals/city/{city}` | Public | Filter hospitals by city |

### 8. Administration (`/api/admin`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/users` | Admin | List all registered user accounts |
| `GET` | `/api/admin/users/patients` | Admin | List patient user accounts |
| `GET` | `/api/admin/users/doctors` | Admin | List doctor user accounts |
| `PUT` | `/api/admin/users/doctors/{id}/verify` | Admin | Approve and verify doctor account |
| `PUT` | `/api/admin/users/{id}/status` | Admin | Toggle user active/inactive status |
| `GET` | `/api/admin/analytics/metrics` | Admin | Key operational metrics summary |
| `GET` | `/api/admin/analytics/dashboard` | Admin | Detailed multi-metric dashboard statistics |
| `GET` | `/api/admin/analytics/trends` | Admin | Appointment volume and revenue trends |
| `GET` | `/api/admin/analytics/export/csv` | Admin | Export analytical report to CSV |
| `GET` | `/api/admin/settings` | Admin | Retrieve dynamic system configuration |
| `POST` | `/api/admin/settings` | Admin | Update system configuration settings |

### 9. Payments (`/api/payments`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/payments/create-intent` | Patient | Create payment transaction intent |
| `POST` | `/api/payments/confirm` | Patient | Confirm consultation payment transaction |

### 10. Notifications (`/api/notifications`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/notifications/user/{userId}` | Authenticated | Retrieve user notifications list |
| `GET` | `/api/notifications/user/{userId}/unread-count` | Authenticated | Get unread notification badge count |
| `POST` | `/api/notifications/{notificationId}/read` | Authenticated | Mark notification as read |
| `POST` | `/api/notifications/user/{userId}/read-all` | Authenticated | Mark all user notifications as read |

---

## Important Setup Notes & Best Practices

1. **Protect Sensitive Secrets:**
   - Never commit `.env` or `application-local.properties` to source control. Both files are strictly excluded in `.gitignore`.
   - In production environments, set `JWT_SECRET`, `DB_USERNAME`, and `DB_PASSWORD` via system environment variables or a secrets manager.

2. **JWT Secret Length Requirement:**
   - JJWT enforces RFC 7518 HS256 requirements: the secret key must be at least 256 bits (32 characters) long. If `JWT_SECRET` is missing or shorter than 32 characters, Spring Boot will throw a configuration or validation error on startup.

3. **File Upload Storage:**
   - Uploaded files are saved to `uploads/records` and `uploads/prescriptions`. These directories are preserved with `.gitkeep`, while actual uploaded documents are ignored to protect Personal Health Information (PHI).

4. **Database Mode (`ddl-auto` & `sql.init.mode`):**
   - In development, `spring.jpa.hibernate.ddl-auto` is set to `update`.
   - `spring.sql.init.mode` is set to `${SQL_INIT_MODE:never}` by default to avoid re-executing schema scripts on every application restart. Set `SQL_INIT_MODE=always` only when initially seeding or creating tables.

---

## License

License terms to be defined by the repository owner.
