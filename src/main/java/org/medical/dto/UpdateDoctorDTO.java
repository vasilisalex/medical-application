package org.medical.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateDoctorDTO {
    @NotBlank
    public String firstName;

    @NotBlank
    public String lastName;

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String specialty;

    @NotBlank
    public String licenseNumber;

    @NotBlank
    public String medicalAssociation;

    @NotBlank
    public String phone;

    @NotBlank
    public String officeStreet;

    @NotBlank
    public String officeCity;

    @NotBlank
    @Pattern(regexp = "\\d{5}", message = "officePostalCode must be exactly 5 digits")
    public String officePostalCode;
}
