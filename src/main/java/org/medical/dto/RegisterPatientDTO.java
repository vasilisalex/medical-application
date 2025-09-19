package org.medical.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * DTO για την καταχώρηση νέου ασθενή.
 */
public class RegisterPatientDTO {
    @NotBlank(message = "AMKA is required")
    @Pattern(regexp = "\\d{11}", message = "AMKA must be exactly 11 digits")
    public String amka;

    @NotBlank(message = "firstName is required")
    public String firstName;

    @NotBlank(message = "lastName is required")
    public String lastName;

    @NotNull(message = "dateOfBirth is required")
    public LocalDate dateOfBirth;

    public String phone;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    public String email;

    @Pattern(regexp = "\\d{9}", message = "AFM must be 9 digits")
    public String afm;

    public String idNumber;
    public String insuranceType;

    public String addressStreet;
    public String addressCity;
    @Pattern(regexp = "\\d{5}", message = "Postal code must be 5 digits")
    public String addressPostalCode;
}
