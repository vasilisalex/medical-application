package org.medical.dto;

import java.time.LocalDate;

/**
 * DTO για καταχώρηση νέου ιατρικού ιστορικού από γιατρό.
 */
public class CreateMedicalRecordDTO {

    public LocalDate date;
    public String sickness;
    public String medication;
    public String exams;
    public String patientAmka;
}