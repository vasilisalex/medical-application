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
    public Response createRecord(@jakarta.validation.Valid CreateMedicalRecordDTO dto) {
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
        record.visitType = dto.visitType;
        record.facility = dto.facility;
        record.doctorSpecialty = dto.doctorSpecialty;
        record.symptoms = dto.symptoms;
        record.diagnosisCode = dto.diagnosisCode;
        record.dosage = dto.dosage;
        record.followUpDate = dto.followUpDate;
        record.notes = dto.notes;
        record.doctor = doctor;
        record.patient = patient;
        record.createdAt = java.time.LocalDateTime.now();
        record.updatedAt = record.createdAt;
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
     * GET /medicalrecords/{id}
     * Επιστρέφει μία εγγραφή ιστορικού βάσει id.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed("doctor")
    public Response getRecordById(@PathParam("id") Long id) {
        MedicalRecord record = MedicalRecord.findById(id);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Record not found").build();
        }
        return Response.ok(record).build();
    }

    /**
     * PUT /medicalrecords/{id}
     * Ενημέρωση υπάρχουσας εγγραφής.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("doctor")
    public Response updateRecord(@PathParam("id") Long id, @jakarta.validation.Valid CreateMedicalRecordDTO dto) {
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
        record.visitType = dto.visitType;
        record.facility = dto.facility;
        record.doctorSpecialty = dto.doctorSpecialty;
        record.symptoms = dto.symptoms;
        record.diagnosisCode = dto.diagnosisCode;
        record.dosage = dto.dosage;
        record.followUpDate = dto.followUpDate;
        record.notes = dto.notes;
        record.patient = patient;

        record.updatedAt = java.time.LocalDateTime.now();
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
        MedicalRecord rec = MedicalRecord.findById(id);
        if (rec == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Medical record not found").build();
        }
        String doctorAmka = identity.getPrincipal().getName();
        String creatorAmka = rec.doctor != null ? rec.doctor.amka : null;
        if (creatorAmka == null || !creatorAmka.equals(doctorAmka)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Only the creating doctor can delete this record").build();
        }
        boolean deleted = MedicalRecord.deleteById(id);
        return deleted ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND)
                    .entity("Medical record not found").build();
    }
}
