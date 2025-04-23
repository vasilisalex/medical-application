package org.medical.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.medical.dto.RegisterPatientDTO;
import org.medical.model.Patient;

import java.util.List;

@Path("/patients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {

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
    public Response register(RegisterPatientDTO dto) {
        Patient patient = new Patient();
        patient.amka = dto.amka;
        patient.firstName = dto.firstName;
        patient.lastName = dto.lastName;
        patient.dateOfBirth = dto.dateOfBirth;
        patient.phone = dto.phone;
        patient.email = dto.email;
        patient.persist();

        return Response.status(Response.Status.CREATED).entity(patient).build();
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
        boolean deleted = Patient.deleteById(id);

        if (deleted) {
            return Response.noContent().build(); // 204 No Content
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Patient not found with id " + id)
                    .build();
        }
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
}
