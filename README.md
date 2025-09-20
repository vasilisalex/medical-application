# Medical App — README & Technical Overview

Εφαρμογή Quarkus για διαχείριση ιατρικών δεδομένων ασθενών από γιατρούς. Παρέχει REST APIs με JWT, αποθήκευση σε PostgreSQL μέσω Hibernate Panache και απλό frontend (static HTML + vanilla JS). Οι κωδικοί αποθηκεύονται ως BCrypt hashes και ισχύουν κανόνες επικύρωσης σε client και server.

—

## Περιγραφή (Features)
- Εγγραφή/Σύνδεση γιατρών (AMKA + password) και έκδοση JWT
- Διαχείριση ασθενών (create/update/delete, αναζητήσεις)
- Διαχείριση ιατρικών περιστατικών (create/read/update/delete)
- Audit logging για κάθε ενέργεια (doctorId, patientAmka, recordId, action, at)
- Επικύρωση δεδομένων (Bean Validation) και ομοιόμορφα JSON errors

—

## Stack
- Backend: Quarkus 3.x, JAX‑RS (RESTEasy Reactive), Hibernate ORM + Panache, SmallRye JWT, Bean Validation, Elytron BCrypt
- DB: PostgreSQL
- Frontend: Static HTML + vanilla JS (fetch + Bearer JWT)
- Build: Maven Wrapper, Quarkus dev mode, JAR packaging, Dockerfiles

—

## Αρχιτεκτονική

### Λογική άποψη
- UI: static σε `src/main/resources/META-INF/resources` (π.χ. `login.html`, `register.html`, `profile.html`). JS κάτω από `.../resources/js` για client-side validation και κλήσεις API.
- API: JAX‑RS resources κάτω από `org.medical.resource` για doctors, patients, medical records, audit logs.
- AuthN/AuthZ: JWT με ρόλο `doctor`; το subject του token είναι το AMKA του γιατρού.
- Persistence: JPA entities με Panache Active Record.

### Ενδεικτική ροή
1) Login → POST `/doctors/login` με AMKA/password → έκδοση JWT
2) Frontend αποθηκεύει το token (localStorage) και το στέλνει σε `Authorization: Bearer ...`
3) Προστατευμένα endpoints ανακτούν τον γιατρό μέσω `SecurityIdentity`

—

## API (JAX‑RS Resources)

### `/doctors` (DoctorResource)
- `GET /hello`: απλό health check
- `POST /register`: δημιουργία γιατρού, έλεγχος confirm password, μοναδικό AMKA/email, αποθήκευση BCrypt hash
- `POST /login`: έλεγχος credential και επιστροφή JWT
- `GET /me`: στοιχεία συνδεδεμένου γιατρού
- `PUT /me`: ενημέρωση προφίλ, έλεγχος διπλού email
- `PUT /me/password`: αλλαγή κωδικού με έλεγχο τρέχοντος και επιβεβαίωσης

### `/patients` (PatientResource)
- `GET /`: λίστα ασθενών
- `POST /`: δημιουργία ασθενή (θέτει `createdBy` τον τρέχοντα γιατρό)
- `PUT /{id}`: ενημέρωση, με έλεγχο AMKA conflict
- `DELETE /{id}`: διαγραφή (σβήνει πρώτα τα medical records)
- `GET /{amka}`: ανάγνωση προφίλ ασθενή βάσει AMKA (με audit READ)
- `GET /search?amka=`: αναζήτηση βάσει AMKA
- `GET /mine`: ασθενείς με τουλάχιστον ένα record από τον τρέχοντα γιατρό
- `GET /search-any?q=`: exact first/last name (case‑insensitive) ή partial AMKA
- `GET /search-adv?amka=&first=&last=`: συνδυαστική αναζήτηση (AND)

### `/medicalrecords` (MedicalRecordResource)
- `POST /`: δημιουργία περιστατικού (σύνδεση σε τρέχοντα doctor και patient μέσω AMKA)
- `GET /patient/{amka}`: όλα τα περιστατικά ασθενή
- `GET /{id}`: ένα περιστατικό
- `PUT /{id}`: ενημέρωση
- `DELETE /{id}`: μόνο ο δημιουργός‑γιατρός μπορεί να διαγράψει (403 αλλιώς)

### `/audit-logs` (AuditLogResource)
- `GET /audit-logs`: λίστα καταγραφών με φίλτρα (`doctorId`, `patientAmka`, `recordId`, `action`, `from`, `to`, `page`, `size`)
- `GET /audit-logs/export`: export CSV με τα ίδια φίλτρα

—

## Entities (JPA/Panache)
- `Doctor`: amka (unique), ονοματεπώνυμο, email (unique), `passwordHash`, specialty, licenseNumber, medicalAssociation, phone, office address
- `Patient`: amka (unique), στοιχεία επικοινωνίας, `createdBy` (ManyToOne Doctor), διεύθυνση, timestamps
- `MedicalRecord`: κλινικά πεδία, `doctor` (ManyToOne Doctor), `patient` (ManyToOne Patient), timestamps
- `AuditLog`: `doctorId`, `patientAmka`, `recordId`, `action`, `at` με indexes

—

## DTOs & Validation
- DTOs: `RegisterDoctorDTO`, `LoginDoctorDTO`, `UpdateDoctorDTO`, `ChangePasswordDTO`, `RegisterPatientDTO`, `CreateMedicalRecordDTO`
- Κανόνες (server‑side Bean Validation):
  - AMKA: `@Pattern("\\d{11}")` (doctor/patient/login & `patientAmka` σε medical record)
  - Phone: `@Pattern("\\d{10}")` (doctor phone)
  - TK: `@Pattern("\\d{5}")` (doctor office & patient address)
  - AFM: `@Pattern("\\d{9}")` (optional πεδίο)
  - Email: `@Email`; required με `@NotBlank`/`@NotNull`
  - Password: regex πολυπλοκότητας (βλ. Security)
- Cross‑field: επιβεβαίωση password ελέγχεται στους resources (όχι custom validator)
- Validation errors: HTTP 400 με `{ errors: [ { field, message } ] }` από `ValidationExceptionMapper`

—

## Security
- Αποθήκευση κωδικών: BCrypt (`io.quarkus.elytron.security.common.BcryptUtil`)
- JWT (SmallRye JWT):
  - Verification key: `src/main/resources/META-INF/publicKey.pem`
  - Signing key: `src/main/resources/META-INF/privateKey.pem` (git‑ignored, να παρέχεται τοπικά)
  - Config: `mp.jwt.verify.publickey.location`, `mp.jwt.verify.issuer`, `smallrye.jwt.sign.key.location`, `mp.jwt.token.lifetime`
- Authorization: `@RolesAllowed("doctor")`
- Claims: subject=AMKA, issuer `medical-app`, role `doctor`, basic name claims

—

## Audit Logging
- `AuditService.log(action, patientAmka, recordId)`: `@Transactional`, αντλεί τον γιατρό από `SecurityIdentity` (JWT), βρίσκει `doctor.id` και κάνει persist την εγγραφή
- Χρήση: σε create/update/delete περιστατικών/ασθενών και σε reads όπου έχει νόημα

—

## Frontend
- Σελίδες: `login.html`, `register.html`, `profile.html`, `dashboard.html`, `patient[-new|-edit].html`, `medicalrecord[-new].html`
- Client validation: AMKA 11 ψηφία (live sanitize), Phone 10, TK 5, password policy με αναλυτικά μηνύματα
- Κλήσεις API με fetch και `Authorization: Bearer <token>` από `localStorage`
- Εμφάνιση σφαλμάτων: δείχνει backend `error` ή το πρώτο validation message από `errors[]`

—

## Error Handling & Conventions
Όλα τα errors γυρίζουν ομοιόμορφο JSON:

```
{ "error": "<code>", "code": "<code>", "message": "<human-friendly>", "path": "/request/path" }
```

Validation (400):

```
{ "error": "validation_error", "code": "validation_error", "message": "Validation failed", "path": "/doctors/register", "errors": [ { "field": "email", "message": "must be a well-formed email address" } ] }
```

Codes: 400 `bad_request|validation_error`, 401 `unauthorized`, 403 `forbidden`, 404 `not_found`, 409 `conflict`

—

## Dev/Test Seed Data
Σε dev και test προφίλ γίνεται seed ελάχιστων δεδομένων για εύκολα demos:
- Doctor A: AMKA `11111111111`, email `doca@example.com`, password `Abcdef1!`
- Doctor B: AMKA `22222222222`, email `docb@example.com`, password `Abcdef1!`
- Patient P: AMKA `99999999999` (με ένα medical record από τον Doctor A)

—

## Postman Collection
- Αρχείο: `postman/medical-app.postman_collection.json`
- Μεταβλητές: `{{base_url}}` (default `http://localhost:8080`), `{{token}}`, `{{patientId}}`, `{{patientAmka}}`, `{{recordId}}` + helpers για seeded data
- Φάκελοι/Κατηγορίες:
  - Doctors (login/register/me/update/password)
  - Patients (CRUD, search, search-adv, profile by AMKA)
  - Medical Records (CRUD)
  - Audit Logs (search, export CSV)
  - Errors (Examples) για 400/401/403/404/409

—

## Scripts
- `scripts/curl-errors.sh`: αυτοματοποιεί κλήσεις που παράγουν τυποποιημένα σφάλματα (400/401/403/404/409). Τρέχει μετά το `mvn quarkus:dev`.

—

## Testing
- Εκτέλεση: `./mvnw test`
- Καλύπτει:
  - ApiExceptionMapper: 400/404/409 με ενιαίο envelope
  - ValidationExceptionMapper: 400 με `errors[]`
  - Security mappers: 401 χωρίς token, 403 forbidden
  - AuditService: ότι γράφει σωστά `doctorId/patientAmka/recordId/action`

## Build, Run, Deploy
- Build: `./mvnw -DskipTests package`
- Dev run: `./mvnw quarkus:dev` (προεπιλογή port 8080, αλλάζει με `-Dquarkus.http.port=8081`)
- Docker: έτοιμα Dockerfiles κάτω από `src/main/docker/`

### Configuration (application.properties)
- DB: `quarkus.datasource.jdbc.url=jdbc:postgresql://<host>:5432/medicaldb`
- JWT: `mp.jwt.verify.publickey.location=META-INF/publicKey.pem`, `smallrye.jwt.sign.key.location=META-INF/privateKey.pem`, `mp.jwt.verify.issuer=medical-app`, `mp.jwt.token.lifetime=3600`
- Hibernate: `%dev.quarkus.hibernate-orm.database.generation=update`, `%test....=drop-and-create`, `quarkus.hibernate-orm.log.sql=true`

—

## Data Model (high level)

```
Doctor (id, amka[unique], firstName, lastName, email[unique], passwordHash, specialty, licenseNumber, medicalAssociation, phone, officeStreet, officeCity, officePostalCode)

Patient (id, amka[unique], firstName, lastName, dateOfBirth, phone, email, afm, idNumber, insuranceType, addressStreet, addressCity, addressPostalCode, createdBy->Doctor, createdAt, updatedAt)

MedicalRecord (id, date, sickness, medication, exams, visitType, facility, doctorSpecialty, symptoms, diagnosisCode, dosage, followUpDate, notes, doctor->Doctor, patient->Patient, createdAt, updatedAt)

AuditLog (id, doctorId[idx], patientAmka[idx], recordId[idx], action, at)
```

—

## Repository Layout
- Backend: `src/main/java/org/medical/...`
- Frontend: `src/main/resources/META-INF/resources/...`
- Config: `src/main/resources/application.properties`
- Keys: `src/main/resources/META-INF/publicKey.pem` (tracked) και `META-INF/privateKey.pem` (git‑ignored)
- Docs: `docs/ERRORS_DEMO.md`

—

## Quick Demo
1) Εκκίνηση DB (PostgreSQL) και ρύθμιση `application.properties`
2) `./mvnw quarkus:dev`
3) Εγγραφή γιατρού από `/register.html` (ισχύουν οι κανόνες password)
4) Login από `/login.html` → `/dashboard.html`
5) Δημιουργία ασθενή και ιατρικού περιστατικού
6) Ενημέρωση προφίλ και αλλαγή κωδικού από `/profile.html`

—

## Σημειώσεις / Μελλοντικά
- Ορισμένα `quarkus.jwt.token.*` keys μπορεί να εμφανιστούν ως μη αναγνωρισμένα, αλλά το SmallRye JWT δουλεύει με τα παραπάνω
- Προσθήκη pagination/filters σε λίστες (patients, records, audits)
- Retention/redaction στις audit εγγραφές αν χρειάζεται
- Προσθήκη integration tests και H2 profile για demos
- E2E smoke tests (Playwright/Cypress) για αυτοματοποίηση demo
