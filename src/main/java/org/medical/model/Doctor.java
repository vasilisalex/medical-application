package org.medical.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Doctor extends PanacheEntity {
    @Column(unique = true)
    public String amka;
    public String firstName;
    public String lastName;    
    public String passwordHash;
}
