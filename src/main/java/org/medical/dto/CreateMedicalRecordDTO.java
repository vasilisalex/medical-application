package org.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * DTO για καταχώρηση νέου ιατρικού ιστορικού από γιατρό.
 */
public class CreateMedicalRecordDTO {

    @NotNull(message = "date is required")
    public LocalDate date;

    @NotBlank(message = "sickness is required")
    public String sickness;

    public String medication;
    public String exams;

    public String visitType;
    public String facility;
    public String doctorSpecialty;
    public String symptoms;
    public String diagnosisCode;
    public String dosage;
    public LocalDate followUpDate;
    public String notes;

    @NotBlank(message = "patientAmka is required")
    @Pattern(regexp = "\\d{11}", message = "patientAmka must be exactly 11 digits")
    public String patientAmka;
}
