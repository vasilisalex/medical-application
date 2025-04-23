package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Οντότητα που αναπαριστά έναν ασθενή στη βάση δεδομένων.
 * Ο ΑΜΚΑ είναι μοναδικός και χρησιμοποιείται για αναζήτηση.
 */
@Entity
public class Patient extends PanacheEntity {

    @Column(unique = true) // Μοναδικός αριθμός για κάθε ασθενή
    public String amka;

    public String firstName;
    public String lastName;
    public LocalDate dateOfBirth;
    public String phone;
    public String email;
}
