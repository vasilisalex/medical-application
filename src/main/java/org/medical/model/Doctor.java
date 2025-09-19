package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="Doctor")
public class Doctor extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String amka;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String specialty;

    @Column(nullable = false)
    public String licenseNumber;

    @Column(nullable = false)
    public String medicalAssociation; 

    @Column(nullable = false)
    public String phone;

    @Column(nullable = false)
    public String officeStreet;

    @Column(nullable = false)
    public String officeCity;
    
    @Column(nullable = false)
    public String officePostalCode;
}
