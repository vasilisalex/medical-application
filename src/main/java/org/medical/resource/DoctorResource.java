package org.medical.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.medical.model.Doctor;
import org.medical.dto.RegisterDoctorDTO;
import org.medical.dto.LoginDoctorDTO;
import io.smallrye.jwt.build.Jwt;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.identity.SecurityIdentity;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@Path("/doctors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DoctorResource {

    @Inject
    SecurityIdentity identity;

    /** 
     * προσωρινο hello endpoint για να περασει το τεστ
     */
    @GET
    @Path("/hello")
    @PermitAll
    public Response hello() {
        return Response.ok("ok").build();
    }  

    /**
     * POST /doctors/register
     * Δημιουργει νεο γιατρο στην βαση.
     * Ελεγχει mismatch passwords και διπλοτυπα amka/email.
     * Επιστρεφει 201 Created με id, amka, email.
     */
    @POST
    @Path("/register")
    @Transactional
    @PermitAll
    public Response register(@jakarta.validation.Valid RegisterDoctorDTO dto) {
        // 1) ελεγχος passwords
        if (!dto.password.equals(dto.confirmPassword)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "to password den tairiazei me tin epivevaiosi"))
                    .build();
        }
        String amka  = dto.amka.trim();
        String email = dto.email.trim().toLowerCase();

        // 2) ελεγχος υπαρξης ιδιου amka ή email
        boolean exists = Doctor.find("amka", amka).count() > 0
                      || Doctor.find("email", email).count() > 0;
        if (exists) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "iparxei idi giatros me auto to AMKA i email"))
                    .build();
        }

        // 3) αποθηκευση του γιατρου
        Doctor doctor = new Doctor();
        doctor.amka         = amka;
        doctor.firstName    = dto.firstName.trim();
        doctor.lastName     = dto.lastName.trim();
        doctor.email        = email;
        doctor.specialty    = dto.specialty.trim();
        doctor.licenseNumber= dto.licenseNumber.trim();
        doctor.medicalAssociation = dto.medicalAssociation.trim();
        doctor.phone        = dto.phone.trim();
        doctor.officeStreet = dto.officeStreet.trim();
        doctor.officeCity   = dto.officeCity.trim();
        doctor.officePostalCode = dto.officePostalCode.trim();
        doctor.passwordHash = BcryptUtil.bcryptHash(dto.password);
        doctor.persist();

        // 4) δημιουργια URI για το Location header
        URI location = URI.create("/doctors/" + doctor.id);
        return Response.created(location)
                .entity(Map.of(
                    "id",    doctor.id,
                    "amka",  doctor.amka,
                    "email", doctor.email
                ))
                .build();
    }

    /**
     * POST /doctors/login
     * Ελεγχει credentials και επιστρεφει JWT token.
     * 404 αν δεν βρεθει γιατρος, 401 αν λαθος password.
     */
    @POST
    @Path("/login")
    @Transactional
    @PermitAll
    public Response login(@jakarta.validation.Valid LoginDoctorDTO dto) {
        // αναζητηση γιατρου με amka
        Doctor doctor = Doctor.find("amka", dto.amka.trim()).firstResult();
        if (doctor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "doctor not found"))
                    .build();
        }
        // ελεγχος κρυπτογραφημενου password
        if (!BcryptUtil.matches(dto.password, doctor.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "invalid password"))
                    .build();
        }
        // δημιουργια JWT με claim ρολου και στοιχειων
        String token = Jwt.claims()
                .subject(doctor.amka)
                .issuer("medical-app")
                .groups("doctor")
                .claim("firstName", doctor.firstName)
                .claim("lastName", doctor.lastName)
                .expiresIn(Duration.ofHours(4))
                .sign();

        // επιστροφη token στον client
        return Response.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .entity(Map.of("token", token))
                .build();
    }

    /**
     * GET /doctors/me
     * Επιστρέφει τα στοιχεία του συνδεδεμένου γιατρού από το JWT (subject=amka).
     */
    @GET
    @Path("/me")
    @RolesAllowed("doctor")
    public Response me() {
        String amka = identity.getPrincipal().getName();
        Doctor d = Doctor.find("amka", amka).firstResult();
        if (d == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(Map.ofEntries(
                Map.entry("id", d.id),
                Map.entry("amka", d.amka),
                Map.entry("firstName", d.firstName),
                Map.entry("lastName", d.lastName),
                Map.entry("email", d.email),
                Map.entry("specialty", d.specialty),
                Map.entry("licenseNumber", d.licenseNumber),
                Map.entry("medicalAssociation", d.medicalAssociation),
                Map.entry("phone", d.phone),
                Map.entry("officeStreet", d.officeStreet),
                Map.entry("officeCity", d.officeCity),
                Map.entry("officePostalCode", d.officePostalCode)
        )).build();
    }

    /**
     * PUT /doctors/me
     * Ενημέρωση προφίλ (firstName, lastName, email).
     */
    @PUT
    @Path("/me")
    @Transactional
    @RolesAllowed("doctor")
    public Response updateMe(@jakarta.validation.Valid org.medical.dto.UpdateDoctorDTO dto) {
        String amka = identity.getPrincipal().getName();
        Doctor d = Doctor.find("amka", amka).firstResult();
        if (d == null) return Response.status(Response.Status.NOT_FOUND).build();

        String newEmail = dto.email.trim().toLowerCase();
        if (!newEmail.equals(d.email)) {
            boolean exists = Doctor.find("email", newEmail).count() > 0;
            if (exists) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "email already in use"))
                        .build();
            }
        }

        d.firstName = dto.firstName.trim();
        d.lastName = dto.lastName.trim();
        d.email = newEmail;
        d.specialty = dto.specialty.trim();
        d.licenseNumber = dto.licenseNumber.trim();
        d.medicalAssociation = dto.medicalAssociation.trim();
        d.phone = dto.phone.trim();
        d.officeStreet = dto.officeStreet.trim();
        d.officeCity = dto.officeCity.trim();
        d.officePostalCode = dto.officePostalCode.trim();
        return Response.ok(Map.ofEntries(
                Map.entry("id", d.id),
                Map.entry("amka", d.amka),
                Map.entry("firstName", d.firstName),
                Map.entry("lastName", d.lastName),
                Map.entry("email", d.email),
                Map.entry("specialty", d.specialty),
                Map.entry("licenseNumber", d.licenseNumber),
                Map.entry("medicalAssociation", d.medicalAssociation),
                Map.entry("phone", d.phone),
                Map.entry("officeStreet", d.officeStreet),
                Map.entry("officeCity", d.officeCity),
                Map.entry("officePostalCode", d.officePostalCode)
        )).build();
    }

    /**
     * PUT /doctors/me/password
     * Αλλαγή κωδικού με έλεγχο τρέχοντος κωδικού.
     */
    @PUT
    @Path("/me/password")
    @Transactional
    @RolesAllowed("doctor")
    public Response changePassword(@jakarta.validation.Valid org.medical.dto.ChangePasswordDTO dto) {
        String amka = identity.getPrincipal().getName();
        Doctor d = Doctor.find("amka", amka).firstResult();
        if (d == null) return Response.status(Response.Status.NOT_FOUND).build();

        if (!BcryptUtil.matches(dto.currentPassword, d.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "current password incorrect"))
                    .build();
        }
        if (!dto.newPassword.equals(dto.confirmPassword)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "password confirmation does not match"))
                    .build();
        }
        d.passwordHash = BcryptUtil.bcryptHash(dto.newPassword);
        return Response.noContent().build();
    }
}
