package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Απλό audit log για GDPR: ποιος γιατρός έκανε τι και πότε.
 * Καταγράφει: doctorId, patientAmka, recordId, action, timestamp.
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_patient_amka", columnList = "patientAmka"),
        @Index(name = "idx_audit_record_id", columnList = "recordId"),
        @Index(name = "idx_audit_doctor_id", columnList = "doctorId")
})
public class AuditLog extends PanacheEntity {

    /**
     * ID του γιατρού που εκτέλεσε την ενέργεια.
     */
    @Column(nullable = false)
    public Long doctorId;

    /**
     * AMKA ασθενή που αφορά η ενέργεια (μπορεί να είναι null αν δεν υπάρχει).
     */
    @Column
    public String patientAmka;

    /**
     * ID ιατρικού ιστορικού (μπορεί να είναι null για patient-level ενέργειες).
     */
    @Column
    public Long recordId;

    /**
     * Ενέργεια: READ / CREATE / UPDATE / DELETE
     */
    @Column(nullable = false)
    public String action;

    /**
     * Χρονική στιγμή της ενέργειας (server local time).
     */
    @Column(nullable = false)
    public java.time.LocalDateTime at;
}

