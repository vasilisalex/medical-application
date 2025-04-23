package org.medical.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.medical.model.Doctor;
import org.medical.dto.RegisterDoctorDTO;
import org.medical.dto.LoginDoctorDTO;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import java.time.Duration;
import io.smallrye.jwt.build.Jwt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Path("/doctors")
@Consumes(MediaType.APPLICATION_JSON)
public class DoctorResource {

    // Εγγραφή γιατρού
    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterDoctorDTO dto) {
        Doctor doctor = new Doctor();
        doctor.amka = dto.amka.trim();
        doctor.firstName = dto.firstName;
        doctor.lastName = dto.lastName;
        doctor.passwordHash = hashPassword(dto.password);
        doctor.persist();

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    @Transactional
    public Response login(LoginDoctorDTO dto) {
        Doctor doctor = Doctor.find("amka", dto.amka.trim()).firstResult();
        if (doctor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Doctor not found with amka: " + dto.amka).build();
        }
    
        String providedHash = hashPassword(dto.password);
        if (!providedHash.equals(doctor.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid password").build();
        }
    
        // Δημιουργούμε JWT token με claims και ρόλο doctor
        String token = Jwt.claims()
            .subject(doctor.amka)
            .issuer("medical-app")
            .groups("doctor") // ρόλος
            .claim("firstName", doctor.firstName)
            .claim("lastName", doctor.lastName)
            .expiresIn(Duration.ofHours(4))
            .sign();
    
        // Επιστρέφουμε token στον client
        return Response.ok()
                .header("Authorization", "Bearer " + token)
                .entity(token)
                .build();
    }
    
    // Μέθοδος για κρυπτογράφηση του password με SHA-256
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
