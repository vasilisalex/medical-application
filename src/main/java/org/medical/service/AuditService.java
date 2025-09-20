package org.medical.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.security.identity.SecurityIdentity;
import org.medical.model.AuditLog;
import org.medical.model.Doctor;

/**
 * Υπηρεσία για καταγραφή ενεργειών σε AuditLog.
 */
@ApplicationScoped
public class AuditService {

    @Inject
    SecurityIdentity identity;

    /**
     * Καταγράφει μία ενέργεια (READ/CREATE/UPDATE/DELETE) για συγκεκριμένο ασθενή/εγγραφή.
     * - Ανακτά τον γιατρό από το JWT (subject = doctor.amka) και σώζει το id.
     * - Επιτρέπει null για patientAmka ή recordId όπου δεν υπάρχει.
     */
    public void log(String action, String patientAmka, Long recordId) {
        String doctorAmka = identity != null && identity.getPrincipal() != null
                ? identity.getPrincipal().getName() : null;

        Doctor doctor = null;
        if (doctorAmka != null && !doctorAmka.isBlank()) {
            doctor = Doctor.find("amka", doctorAmka).firstResult();
        }

        AuditLog al = new AuditLog();
        al.action = action;
        al.patientAmka = patientAmka;
        al.recordId = recordId;
        al.doctorId = doctor != null ? doctor.id : null;
        al.at = java.time.LocalDateTime.now();
        al.persist();
    }
}

