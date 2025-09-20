package org.medical.bootstrap;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.medical.model.Doctor;
import org.medical.model.MedicalRecord;
import org.medical.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ApplicationScoped
@Startup
public class DevTestDataSeeder {

    /**
     * Seeds minimal demo data in dev and test profiles.
     */
    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (LaunchMode.current() == LaunchMode.NORMAL) {
            return; // do not seed in prod
        }

        // Doctors A and B
        String amkaA = "11111111111";
        String amkaB = "22222222222";

        Doctor a = Doctor.find("amka", amkaA).firstResult();
        if (a == null) {
            a = new Doctor();
            a.amka = amkaA;
            a.firstName = "Alice";
            a.lastName = "Demo";
            a.email = "doca@example.com";
            a.passwordHash = BcryptUtil.bcryptHash("Abcdef1!");
            a.specialty = "Cardiology";
            a.licenseNumber = "LIC-A";
            a.medicalAssociation = "Assoc-A";
            a.phone = "2100000000";
            a.officeStreet = "Alpha 1";
            a.officeCity = "Athens";
            a.officePostalCode = "12345";
            a.persist();
        }

        Doctor b = Doctor.find("amka", amkaB).firstResult();
        if (b == null) {
            b = new Doctor();
            b.amka = amkaB;
            b.firstName = "Bob";
            b.lastName = "Demo";
            b.email = "docb@example.com";
            b.passwordHash = BcryptUtil.bcryptHash("Abcdef1!");
            b.specialty = "Dermatology";
            b.licenseNumber = "LIC-B";
            b.medicalAssociation = "Assoc-B";
            b.phone = "2100000001";
            b.officeStreet = "Beta 2";
            b.officeCity = "Athens";
            b.officePostalCode = "12346";
            b.persist();
        }

        // Patient P
        String amkaP = "99999999999";
        Patient p = Patient.find("amka", amkaP).firstResult();
        if (p == null) {
            p = new Patient();
            p.amka = amkaP;
            p.firstName = "Pat";
            p.lastName = "Demo";
            p.phone = "2101234567";
            p.email = "pat@example.com";
            p.addressStreet = "Gamma 3";
            p.addressCity = "Athens";
            p.addressPostalCode = "11111";
            p.createdAt = LocalDateTime.now();
            p.updatedAt = p.createdAt;
            p.createdBy = a;
            p.persist();
        }

        // One medical record created by Doctor A for P (if none exists)
        boolean hasRecord = MedicalRecord.find("patient.amka = ?1 and doctor.amka = ?2", amkaP, amkaA).count() > 0;
        if (!hasRecord) {
            MedicalRecord r = new MedicalRecord();
            r.date = LocalDate.now();
            r.sickness = "Common cold";
            r.medication = "Rest";
            r.exams = "None";
            r.visitType = "Office";
            r.facility = "Clinic";
            r.doctorSpecialty = a.specialty;
            r.symptoms = "Cough";
            r.diagnosisCode = "J00";
            r.dosage = "N/A";
            r.followUpDate = LocalDate.now().plusDays(7);
            r.notes = "Seeded record";
            r.doctor = a;
            r.patient = p;
            r.createdAt = LocalDateTime.now();
            r.updatedAt = r.createdAt;
            r.persist();
        }
    }
}

