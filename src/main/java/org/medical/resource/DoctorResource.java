package org.medical.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.medical.model.Doctor;
import org.medical.dto.RegisterDoctorDTO;
import org.medical.dto.LoginDoctorDTO;
import io.smallrye.jwt.build.Jwt;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.util.Map;

import jakarta.annotation.security.PermitAll;

@Path("/doctors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DoctorResource {

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
    public Response register(RegisterDoctorDTO dto) {
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
        doctor.passwordHash = hashPassword(dto.password);
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
    public Response login(LoginDoctorDTO dto) {
        // αναζητηση γιατρου με amka
        Doctor doctor = Doctor.find("amka", dto.amka.trim()).firstResult();
        if (doctor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "doctor not found"))
                    .build();
        }
        // ελεγχος κρυπτογραφημενου password
        String providedHash = hashPassword(dto.password);
        if (!providedHash.equals(doctor.passwordHash)) {
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

    // βοηθητικη μεθοδος για SHA-256 κρυπτογραφηση password
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash password", e);
        }
    }
}