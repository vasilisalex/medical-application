package org.medical.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.medical.dto.CreateMedicalRecordDTO;
import org.medical.model.Doctor;
import org.medical.model.MedicalRecord;
import org.medical.model.Patient;
import io.quarkus.security.identity.SecurityIdentity;

import java.util.List;

@Path("/medicalrecords")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalRecordResource {

    @Inject
    SecurityIdentity identity; // Παίρνει το JWT token του χρήστη

    /**
     * POST /medicalrecords
     * Δημιουργεί νέο ιατρικό περιστατικό.
     */
    @POST
    @Transactional
    @RolesAllowed("doctor")
    public Response createRecord(CreateMedicalRecordDTO dto) {
        String doctorAmka = identity.getPrincipal().getName();

        Doctor doctor = Doctor.find("amka", doctorAmka).firstResult();
        if (doctor == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Doctor not found").build();
        }

        Patient patient = Patient.find("amka", dto.patientAmka).firstResult();
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }

        MedicalRecord record = new MedicalRecord();
        record.date = dto.date;
        record.sickness = dto.sickness;
        record.medication = dto.medication;
        record.exams = dto.exams;
        record.doctor = doctor;
        record.patient = patient;
        record.persist();

        return Response.status(Response.Status.CREATED).entity(record).build();
    }

    /**
     * GET /medicalrecords/patient/{amka}
     * Επιστρέφει το ιατρικό ιστορικό ενός ασθενή βάσει AMKA.
     */
    @GET
    @Path("/patient/{amka}")
    @RolesAllowed("doctor")
    public Response getRecordsByPatient(@PathParam("amka") String amka) {
        List<MedicalRecord> records = MedicalRecord.find("patient.amka", amka).list();

        if (records.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No medical records found for patient with AMKA: " + amka).build();
        }

        return Response.ok(records).build();
    }

    /**
     * PUT /medicalrecords/{id}
     * Ενημέρωση υπάρχουσας εγγραφής.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("doctor")
    public Response updateRecord(@PathParam("id") Long id, CreateMedicalRecordDTO dto) {
        MedicalRecord record = MedicalRecord.findById(id);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Record not found").build();
        }

        Patient patient = Patient.find("amka", dto.patientAmka).firstResult();
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }

        record.date = dto.date;
        record.sickness = dto.sickness;
        record.medication = dto.medication;
        record.exams = dto.exams;
        record.patient = patient;

        return Response.ok(record).build();
    }

    /**
     * DELETE /medicalrecords/{id}
     * Διαγραφή περιστατικού.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("doctor")
    public Response deleteRecord(@PathParam("id") Long id) {
        boolean deleted = MedicalRecord.deleteById(id);

        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Medical record not found").build();
        }
    }
}
