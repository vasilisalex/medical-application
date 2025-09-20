# Medical App — Technical Overview & Architecture

## 1. Summary
A Quarkus-based medical records application with a lightweight, server-rendered frontend (static HTML + vanilla JS). It provides authentication for doctors, patient management, and medical records CRUD via REST APIs secured with JWT. Persistence is handled by Hibernate ORM with Panache on PostgreSQL. Passwords are stored as BCrypt hashes and validated for complexity both on the client and server.

Stack highlights:
- Backend: Quarkus 3.x, JAX‑RS (RESTEasy Reactive), Hibernate ORM + Panache, SmallRye JWT, Bean Validation (Jakarta), BCrypt (Elytron)
- DB: PostgreSQL (configurable); dev/test generation strategy via Quarkus
- Frontend: Static pages + vanilla JS, fetch‑based calls with Bearer JWT
- Packaging: Maven Wrapper, Quarkus dev mode, JAR packaging, Dockerfiles available

---

## 2. Architecture

### 2.1 Logical View
- UI: Static pages served from `src/main/resources/META-INF/resources` (e.g., `login.html`, `register.html`, `dashboard.html`, `profile.html`). JS files under `.../resources/js` perform client‑side validation and call REST endpoints.
- API: JAX‑RS endpoints under `org.medical.resource` expose doctor, patient, and medical-record operations.
- AuthN/AuthZ: Login issues a JWT; protected routes annotated with `@RolesAllowed("doctor")`. The authenticated doctor’s AMKA is used as the principal/subject.
- Persistence: Entities under `org.medical.model` mapped with JPA; Panache provides active‑record patterns for simple queries.
- Validation & Errors: DTOs annotated with Jakarta Bean Validation; a global `ExceptionMapper` returns structured validation errors.

### 2.2 Request Flow (example)
1) User logs in from `login.html` → `login.js` → POST `/doctors/login` with AMKA/password.
2) Backend validates, checks BCrypt hash, and issues a signed JWT (`io.smallrye.jwt.build.Jwt`).
3) Frontend stores JWT to `localStorage` and uses it in `Authorization: Bearer ...` for subsequent calls.
4) Protected endpoints read the identity from JWT via `SecurityIdentity`; business logic uses the doctor’s AMKA.

---

## 3. Backend Design

### 3.1 Resources (JAX‑RS)
- `DoctorResource` (`/doctors`)
  - `POST /register`: create doctor; checks password confirmation and uniqueness of AMKA/email; saves BCrypt hash.
  - `POST /login`: verify credentials; return JWT with role `doctor` and basic claims.
  - `GET /me`: return current doctor’s profile (from JWT subject=AMKA).
  - `PUT /me`: update profile; prevents duplicate email.
  - `PUT /me/password`: change password; verifies current password; enforces confirmation.
- `PatientResource` (`/patients`)
  - `GET /`: list all patients (doctor only)
  - `POST /`: create patient; sets `createdBy` to current doctor
  - `PUT /{id}`: update with AMKA conflict check
  - `DELETE /{id}`: delete patient (and dependent medical records first)
  - `GET /search?amka=`: find by AMKA
  - `GET /mine`: patients that have at least one record by the current doctor
  - `GET /search-any?q=`: exact first/last name (case‑insensitive) or partial AMKA
  - `GET /search-adv?amka=&first=&last=`: AND-combined optional criteria
- `MedicalRecordResource` (`/medicalrecords`)
  - `POST /`: create record (links to current doctor and a patient by AMKA)
  - `GET /patient/{amka}`: patient’s records list
  - `GET /{id}`: single record
  - `PUT /{id}`: update record
  - `DELETE /{id}`: only the creating doctor may delete (403 otherwise)

### 3.2 Entities (JPA/Panache)
- `Doctor`: `amka` (unique), names, email (unique), `passwordHash`, contact/office details.
- `Patient`: `amka` (unique), demographics, contact, `createdBy` (`ManyToOne Doctor`), address, timestamps.
- `MedicalRecord`: clinical fields (diagnosis, medication, notes etc.), `doctor` (`ManyToOne Doctor`), `patient` (`ManyToOne Patient`), timestamps.

All entities extend `PanacheEntity` (provides `id`, helpers like `find`, `list`, `persist`).

### 3.3 DTOs & Validation
- DTOs: `RegisterDoctorDTO`, `LoginDoctorDTO`, `UpdateDoctorDTO`, `ChangePasswordDTO`, `RegisterPatientDTO`, `CreateMedicalRecordDTO`.
- Validation examples:
  - AMKA/phone/postal codes via `@Pattern`
  - Email via `@Email`
  - Password policy via `@Pattern` (see Security below)
- `ValidationExceptionMapper` produces `{ "errors": [ { "field": ..., "message": ... } ] }` with HTTP 400.

### 3.4 Security
- Password storage: BCrypt via `io.quarkus.elytron.security.common.BcryptUtil`.
- Authentication: JWT (SmallRye JWT). Keys in `src/main/resources/META-INF/privateKey.pem` and `publicKey.pem`.
- Authorization: `@RolesAllowed("doctor")` on protected endpoints.
- JWT claims: subject (doctor AMKA), issuer `medical-app`, role `doctor`, plus basic name claims.

Password policy (frontend + backend enforced):
- At least 8 characters
- Must contain lowercase, uppercase, number, and special character
- ASCII printable only, no spaces (rejects non‑ASCII e.g., Greek characters)
- Server regex: `^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])[!-~]{8,}$`

### 3.5 Persistence & Config
- `application.properties`:
  - `quarkus.datasource.*` for PostgreSQL connection
  - Hibernate generation: `update` in dev, `drop-and-create` in test
  - JWT: key locations and issuer
- SQL logging enabled for transparency during dev.

---

## 4. Frontend Design

### 4.1 Pages
- `login.html` + `js/login.js`: AMKA input sanitization/validation, login flow, JWT storage.
- `register.html` + `js/register.js`: AMKA/phone/postal sanitization, password policy with per‑field messages, registration flow.
- `profile.html` + `js/profile.js`: view/update doctor data, change password with the same policy and clear field‑level feedback.
- `dashboard.html`, `patient.html`, `patient-new.html` (+ respective JS): patient listing/search, record creation and navigation.

### 4.2 Client Validation
- AMKA: exactly 11 digits (sanitized live)
- Phone: 10 digits; Postal code: 5 digits
- Password: Instant feedback below fields. Example message: “Μη έγκυρος κωδικός. Λείπουν: κεφαλαία, ειδικός χαρακτήρας.”

### 4.3 API Access
- Fetch with `Authorization: Bearer <token>` from `localStorage`.
- Error display: shows backend `error` or first validation message from `errors[]`.

---

## 5. Error Handling & Conventions
- Success: JSON payloads; create/update return entities or minimal maps (e.g., created doctor fields after register).
- Errors:
  - 400: validation errors as `{ errors: [ { field, message } ] }`
  - 401/403/404 where appropriate with `{ error: "..." }`
- Consistent trimming/lowercasing for email; AMKA uniqueness enforced.

---

## 6. Build, Run, Deploy

### 6.1 Build
- `./mvnw -DskipTests package`

### 6.2 Dev Run
- `./mvnw quarkus:dev`
- Default port 8080; can be changed with `-Dquarkus.http.port=8081`.

### 6.3 Configuration
- DB: `quarkus.datasource.jdbc.url=jdbc:postgresql://<host>:5432/medicaldb`
- Keys: `META-INF/privateKey.pem` and `META-INF/publicKey.pem`
- Issuer: `mp.jwt.verify.issuer=medical-app`

### 6.4 Docker (optional)
- Dockerfiles under `src/main/docker/` for JVM/legacy jar.
- Pass DB and JWT settings via env vars or mounted config.

---

## 7. Security Considerations
- BCrypt for credential storage; rate limiting/captcha can be added on `/login` if exposed publicly.
- JWT in `localStorage`: convenient, but vulnerable to XSS; CSP and secure coding of JS/HTML are important.
- CORS not required for same-origin. Enable and scope it properly if needed.
- Input validation both client and server; server regex blocks non‑ASCII passwords to avoid ambiguous unicode and ensure consistent hashing/validation.

---

## 8. Data Model (high level)
```
Doctor (id, amka[unique], firstName, lastName, email[unique], passwordHash, specialty, licenseNumber, medicalAssociation, phone, officeStreet, officeCity, officePostalCode)

Patient (id, amka[unique], firstName, lastName, dateOfBirth, phone, email, afm, idNumber, insuranceType, addressStreet, addressCity, addressPostalCode, createdBy->Doctor, createdAt, updatedAt)

MedicalRecord (id, date, sickness, medication, exams, visitType, facility, doctorSpecialty, symptoms, diagnosisCode, dosage, followUpDate, notes, doctor->Doctor, patient->Patient, createdAt, updatedAt)
```

---

## 9. Known Notes / Future Enhancements
- Config keys: Some `quarkus.jwt.*` keys log as unrecognized in current Quarkus version; behavior is still correct via SmallRye JWT defaults. Consider aligning to the latest config properties.
- Add pagination/filtering for patient and record lists.
- Add auditing (who changed what/when) and soft deletes.
- Introduce integration tests for critical flows (register, login, CRUD).
- Consider H2 profile for easier local demos without PostgreSQL.
- Add E2E smoke tests (Playwright/Cypress) for demo automation.

---

## 10. Repository Layout
- Backend code: `src/main/java/org/medical/...`
- Frontend assets: `src/main/resources/META-INF/resources/...`
- Configuration: `src/main/resources/application.properties`
- Keys: `src/main/resources/META-INF/privateKey.pem`, `publicKey.pem`
- Docs: `docs/TECHNICAL_OVERVIEW.md`

---

## 11. Quick Demo Script (manual)
1) Run DB (Postgres) and configure `application.properties`.
2) `./mvnw quarkus:dev`
3) Open `/register.html`, create a doctor (password policy enforced).
4) Login `/login.html`, land on `/dashboard.html`.
5) Add a patient and create a medical record.
6) Update profile and change password from `/profile.html`.

---

## 12. Contact Points in Code
- Password policy (server): `org.medical.dto.RegisterDoctorDTO`, `ChangePasswordDTO`
- Password policy (client): `META-INF/resources/js/register.js`, `js/profile.js`
- BCrypt & JWT issuance: `org.medical.resource.DoctorResource`
- Validation mapping: `org.medical.error.ValidationExceptionMapper`

### 3.2 Standardized Error Responses

All API errors return a uniform JSON envelope handled by ExceptionMappers:

General shape:

```
{
  "error": "<code>",
  "message": "<human-friendly message>",
  "path": "/request/path"
}
```

Validation (400) additionally includes field errors:

```
{
  "error": "validation_error",
  "message": "Validation failed",
  "path": "/doctors/register",
  "errors": [ { "field": "email", "message": "must be a well-formed email address" } ]
}
```

Mapped status codes and codes:

- 400: `bad_request` or `validation_error`
- 401: `unauthorized`
- 403: `forbidden`
- 404: `not_found`
- 409: `conflict`

Examples:

- 401 (login wrong password):
  `{ "error": "unauthorized", "message": "invalid password", "path": "/doctors/login" }`
- 404 (unknown patient):
  `{ "error": "not_found", "message": "No patient found with AMKA: 123...", "path": "/patients/search" }`
- 409 (duplicate email):
  `{ "error": "conflict", "message": "email already in use", "path": "/doctors/me" }`

Implementation:

- Helper: `org.medical.error.ApiException`
- Mappers: `ApiExceptionMapper`, `ValidationExceptionMapper`, `BadRequestExceptionMapper`, `NotFoundExceptionMapper`, `SecurityExceptionMappers`, `AuthzExceptionMappers`

- Entities: `org.medical.model.*`
- API: `org.medical.resource.*`
