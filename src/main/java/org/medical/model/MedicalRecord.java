package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class MedicalRecord extends PanacheEntity {

    public LocalDate date;

    public String sickness;
    public String medication;
    public String exams;
    public String visitType;
    public String facility;
    public String doctorSpecialty;
    @Column(length = 2000)
    public String symptoms;
    public String diagnosisCode;
    public String dosage;
    public LocalDate followUpDate;
    @Column(length = 4000)
    public String notes;

    @ManyToOne
    public Doctor doctor;

    @ManyToOne
    public Patient patient;

    public java.time.LocalDateTime createdAt;
    public java.time.LocalDateTime updatedAt;
}
