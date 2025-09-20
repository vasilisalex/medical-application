package org.medical.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterDoctorDTO {
    @NotBlank(message = "AMKA is required")
    @Pattern(regexp = "\\d{11}", message = "AMKA must be exactly 11 digits")
    public String amka;

    @NotBlank(message = "firstName is required")
    public String firstName;

    @NotBlank(message = "lastName is required")
    public String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    public String email;

    @NotBlank(message = "password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])[!-~]{8,}$",
        message = "Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες, να περιέχει πεζά, κεφαλαία, αριθμό και ειδικό χαρακτήρα και να αποτελείται από λατινικούς χαρακτήρες ASCII (χωρίς κενά)"
    )
    public String password;

    @NotBlank(message = "confirmPassword is required")
    public String confirmPassword;

    @NotBlank(message = "specialty is required")
    public String specialty;
    @NotBlank(message = "licenseNumber is required")
    public String licenseNumber;
    @NotBlank(message = "medicalAssociation is required")
    public String medicalAssociation;
    @NotBlank(message = "phone is required")
    @Pattern(regexp = "\\d{10}", message = "phone must be exactly 10 digits")
    public String phone;
    @NotBlank(message = "officeStreet is required")
    public String officeStreet;
    @NotBlank(message = "officeCity is required")
    public String officeCity;
    @NotBlank(message = "officePostalCode is required")
    @Pattern(regexp = "\\d{5}", message = "officePostalCode must be exactly 5 digits")
    public String officePostalCode;
}
