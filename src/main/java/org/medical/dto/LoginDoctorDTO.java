package org.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginDoctorDTO {
    @NotBlank(message = "AMKA is required")
    @Pattern(regexp = "\\d{11}", message = "AMKA must be exactly 11 digits")
    public String amka;

    @NotBlank(message = "password is required")
    public String password;
}
