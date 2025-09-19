package org.medical.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.medical.dto.RegisterPatientDTO;
import org.medical.model.Patient;
import org.medical.model.Doctor;
import io.quarkus.security.identity.SecurityIdentity;

import java.util.List;

@Path("/patients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {

    @Inject
    SecurityIdentity identity;

    /**
     * GET /patients
     * Επιστρέφει όλους τους ασθενείς από τη βάση.
     * Προσβάσιμο μόνο από χρήστες με ρόλο "doctor".
     */
    @GET
    @RolesAllowed("doctor")
    public List<Patient> getAllPatients() {
        return Patient.listAll();
    }

    /**
     * POST /patients
     * Δημιουργεί έναν νέο ασθενή βάσει των στοιχείων που στέλνει ο client.
     * Προστατεύεται και απαιτεί αυθεντικοποιημένο χρήστη με ρόλο "doctor".
     */
    @POST
    @Transactional
    @RolesAllowed("doctor")
    public Response register(@jakarta.validation.Valid RegisterPatientDTO dto) {
        // γιατρός που κάνει την καταχώρηση
        String doctorAmka = identity.getPrincipal().getName();
        Doctor creator = Doctor.find("amka", doctorAmka).firstResult();
        Patient patient = new Patient();
        patient.amka = dto.amka;
        patient.firstName = dto.firstName;
        patient.lastName = dto.lastName;
        patient.dateOfBirth = dto.dateOfBirth;
        patient.phone = dto.phone;
        patient.email = dto.email;
        patient.afm = dto.afm;
        patient.idNumber = dto.idNumber;
        patient.insuranceType = dto.insuranceType;
        patient.addressStreet = dto.addressStreet;
        patient.addressCity = dto.addressCity;
        patient.addressPostalCode = dto.addressPostalCode;
        patient.createdAt = java.time.LocalDateTime.now();
        patient.updatedAt = patient.createdAt;
        patient.createdBy = creator;
        patient.persist();

        return Response.status(Response.Status.CREATED).entity(patient).build();
    }

    /**
     * PUT /patients/{id}
     * Ενημέρωση στοιχείων ασθενή.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("doctor")
    public Response updatePatient(@PathParam("id") Long id, @jakarta.validation.Valid RegisterPatientDTO dto) {
        Patient patient = Patient.findById(id);
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Patient not found with id " + id)
                    .build();
        }

        if (!patient.amka.equals(dto.amka)) {
            boolean exists = Patient.find("amka", dto.amka).count() > 0;
            if (exists) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Another patient already uses this AMKA")
                        .build();
            }
        }

        patient.amka = dto.amka;
        patient.firstName = dto.firstName;
        patient.lastName = dto.lastName;
        patient.dateOfBirth = dto.dateOfBirth;
        patient.phone = dto.phone;
        patient.email = dto.email;
        patient.afm = dto.afm;
        patient.idNumber = dto.idNumber;
        patient.insuranceType = dto.insuranceType;
        patient.addressStreet = dto.addressStreet;
        patient.addressCity = dto.addressCity;
        patient.addressPostalCode = dto.addressPostalCode;
        patient.updatedAt = java.time.LocalDateTime.now();

        return Response.ok(patient).build();
    }

    /**
     * DELETE /patients/{id}
     * Διαγράφει έναν ασθενή από τη βάση με βάση το id.
     * Αν δεν βρεθεί, επιστρέφει 404.
     * Χρήση μόνο από γιατρούς.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("doctor")
    public Response deletePatientById(@PathParam("id") Long id) {
        Patient patient = Patient.findById(id);
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Patient not found with id " + id)
                    .build();
        }
        // Πρώτα διαγράφουμε όλα τα ιατρικά περιστατικά του ασθενή ώστε να μην σπάσει το FK
        org.medical.model.MedicalRecord.delete("patient.id", id);
        // Έπειτα διαγράφουμε τον ασθενή
        Patient.deleteById(id);
        return Response.noContent().build();
    }

    /**
     * GET /patients/search?amka=...
     * Αναζητά και επιστρέφει έναν ασθενή βάσει του AMKA.
     * Αν δεν βρεθεί, επιστρέφει 404.
     * Μόνο για γιατρούς με έγκυρο JWT token.
     */
    @GET
    @Path("/search")
    @RolesAllowed("doctor")
    public Response getPatientByAmka(@QueryParam("amka") String amka) {
        Patient patient = Patient.find("amka", amka.trim()).firstResult();

        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No patient found with AMKA: " + amka)
                    .build();
        }

        return Response.ok(patient).build();
    }

    /**
     * GET /patients/mine
     * Επιστρέφει ασθενείς για τους οποίους ο τρέχων γιατρός
     * έχει καταχωρήσει τουλάχιστον ένα ιατρικό περιστατικό.
     */
    @GET
    @Path("/mine")
    @RolesAllowed("doctor")
    public List<Patient> getMyPatients() {
        String doctorAmka = identity.getPrincipal().getName();
        return Patient.list("id in (select distinct m.patient.id from MedicalRecord m where m.doctor.amka = ?1)", doctorAmka);
    }

    /**
     * GET /patients/search-any?q=...
     * Επιστρέφει λίστα ασθενών που ταιριάζουν στο q:
     * - AMKA (ακριβές ή μερικό)
     * - ΑΦΜ (ακριβές ή μερικό)
     * - Όνομα / Επώνυμο (contains, case-insensitive)
     */
    @GET
    @Path("/search-any")
    @RolesAllowed("doctor")
    public List<Patient> searchAny(@QueryParam("q") String q) {
        if (q == null) return java.util.Collections.emptyList();
        String term = q.trim();
        if (term.isEmpty()) return java.util.Collections.emptyList();

        String upper = term.toUpperCase(java.util.Locale.ROOT);
        String likeDigits = "%" + term + "%";

        // Αναζήτηση μόνο με AMKA ή ακριβές Όνομα/Επώνυμο (case-insensitive).
        // Αφαιρέθηκε το κριτήριο ΑΦΜ.
        return Patient.list(
                "upper(firstName) = ?1 or upper(lastName) = ?1 or amka like ?2",
                upper, likeDigits
        );
    }

    /**
     * GET /patients/search-adv?amka=&first=&last=
     * Συνδυαστική αναζήτηση με προαιρετικά κριτήρια.
     * - amka: ακριβές ταίριασμα
     * - first: ακριβές (case-insensitive)
     * - last: ακριβές (case-insensitive)
     * Επιστρέφει όσους ικανοποιούν όλα τα δοσμένα κριτήρια (AND).
     */
    @GET
    @Path("/search-adv")
    @RolesAllowed("doctor")
    public List<Patient> searchAdvanced(@QueryParam("amka") String amka,
                                        @QueryParam("first") String first,
                                        @QueryParam("last") String last) {
        boolean hasAmka = amka != null && !amka.trim().isEmpty();
        boolean hasFirst = first != null && !first.trim().isEmpty();
        boolean hasLast = last != null && !last.trim().isEmpty();
        if (!hasAmka && !hasFirst && !hasLast) return java.util.Collections.emptyList();

        StringBuilder q = new StringBuilder();
        java.util.List<Object> params = new java.util.ArrayList<>();
        boolean whereAdded = false;
        if (hasAmka) {
            q.append(whereAdded ? " and " : "");
            q.append("amka = ?1");
            params.add(amka.trim());
            whereAdded = true;
        }
        if (hasFirst) {
            q.append(whereAdded ? " and " : "");
            q.append("upper(firstName) = ?" + (params.size()+1));
            params.add(first.trim().toUpperCase(java.util.Locale.ROOT));
            whereAdded = true;
        }
        if (hasLast) {
            q.append(whereAdded ? " and " : "");
            q.append("upper(lastName) = ?" + (params.size()+1));
            params.add(last.trim().toUpperCase(java.util.Locale.ROOT));
        }

        String jpql = q.toString();
        return Patient.list(jpql, params.toArray());
    }
}
