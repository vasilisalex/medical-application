package org.medical.dto;

import java.time.LocalDate;

/**
 * DTO για την καταχώρηση νέου ασθενή.
 * Περιλαμβάνει μόνο τα απαραίτητα πεδία που δέχεται η εφαρμογή από τον client.
 */
public class RegisterPatientDTO {
    public String amka;
    public String firstName;
    public String lastName;
    public LocalDate dateOfBirth;
    public String phone;
    public String email;
}
