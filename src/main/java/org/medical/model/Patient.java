package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Patient extends PanacheEntity {

    @Column(unique = true) // μοναδικός αριθμός για κάθε ασθενή
    public String amka;

    public String firstName;
    public String lastName;
    public LocalDate dateOfBirth;
    public String phone;
    public String email;

    // για παρακολούθηση ποιος γιατρός δημιούργησε τον ασθενή
    @ManyToOne
    public Doctor createdBy;

    public String afm;                 // ΑΦΜ (9 ψηφία)
    public String idNumber;            // Αριθμός ταυτότητας
    public String insuranceType;       // "public" / "private"

    // Διεύθυνση κατοικίας
    public String addressStreet;
    public String addressCity;
    public String addressPostalCode;   // ΤΚ (5 ψηφία)

    // timestamps
    public java.time.LocalDateTime createdAt;
    public java.time.LocalDateTime updatedAt;
}
