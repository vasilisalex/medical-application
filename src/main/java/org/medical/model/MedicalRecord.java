package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Οντότητα που αποθηκεύει ιατρικά περιστατικά για κάθε ασθενή.
 */
@Entity
public class MedicalRecord extends PanacheEntity {

    public LocalDate date;

    public String sickness;
    public String medication;
    public String exams;

    @ManyToOne
    public Doctor doctor;

    @ManyToOne
    public Patient patient;
}
