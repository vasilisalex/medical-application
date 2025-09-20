package org.medical.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.medical.model.AuditLog;
import org.medical.model.Doctor;
import io.quarkus.security.identity.SecurityIdentity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/audit-logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuditLogResource {

    @Inject
    SecurityIdentity identity;

    /**
     * GET /audit-logs
     * Επιστρέφει καταγραφές audit βάση φίλτρων. Από προεπιλογή περιορίζει στον τρέχοντα γιατρό.
     */
    @GET
    @RolesAllowed("doctor")
    public List<AuditLog> search(
            @QueryParam("doctorId") Long doctorId,
            @QueryParam("patientAmka") String patientAmka,
            @QueryParam("recordId") Long recordId,
            @QueryParam("action") String action,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {
        // Περιορισμός στον τρέχοντα γιατρό αν δεν δοθεί doctorId
        if (doctorId == null) {
            Long currentDoctorId = getCurrentDoctorId();
            if (currentDoctorId != null) doctorId = currentDoctorId;
        }

        LocalDateTime fromTs = parseDateTime(from, true);
        LocalDateTime toTs = parseDateTime(to, false);

        QueryParts qp = buildQuery(doctorId, patientAmka, recordId, action, fromTs, toTs);
        return AuditLog.find(qp.jpql, qp.params.toArray())
                .page(page, size)
                .list();
    }

    /**
     * GET /audit-logs/export
     * Επιστρέφει CSV (text/csv) με τα ίδια φίλτρα.
     */
    @GET
    @Path("/export")
    @Produces("text/csv;charset=UTF-8")
    @RolesAllowed("doctor")
    public Response exportCsv(
            @QueryParam("doctorId") Long doctorId,
            @QueryParam("patientAmka") String patientAmka,
            @QueryParam("recordId") Long recordId,
            @QueryParam("action") String action,
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        if (doctorId == null) {
            Long currentDoctorId = getCurrentDoctorId();
            if (currentDoctorId != null) doctorId = currentDoctorId;
        }

        LocalDateTime fromTs = parseDateTime(from, true);
        LocalDateTime toTs = parseDateTime(to, false);

        QueryParts qp = buildQuery(doctorId, patientAmka, recordId, action, fromTs, toTs);
        List<AuditLog> rows = AuditLog.find(qp.jpql, qp.params.toArray()).list();

        StringBuilder sb = new StringBuilder();
        sb.append("doctorId,patientAmka,recordId,action,at\n");
        for (AuditLog a : rows) {
            sb.append(z(a.doctorId)).append(',')
              .append(escapeCsv(a.patientAmka)).append(',')
              .append(z(a.recordId)).append(',')
              .append(escapeCsv(a.action)).append(',')
              .append(a.at != null ? a.at.toString() : "").append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment; filename=audits.csv")
                .build();
    }

    private static class QueryParts {
        String jpql;
        List<Object> params;
    }

    private QueryParts buildQuery(Long doctorId, String patientAmka, Long recordId, String action,
                                  LocalDateTime fromTs, LocalDateTime toTs) {
        StringBuilder q = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if (doctorId != null) {
            q.append(" and doctorId = ?").append(params.size() + 1);
            params.add(doctorId);
        }
        if (patientAmka != null && !patientAmka.isBlank()) {
            q.append(" and patientAmka = ?").append(params.size() + 1);
            params.add(patientAmka.trim());
        }
        if (recordId != null) {
            q.append(" and recordId = ?").append(params.size() + 1);
            params.add(recordId);
        }
        if (action != null && !action.isBlank()) {
            q.append(" and upper(action) = ?").append(params.size() + 1);
            params.add(action.trim().toUpperCase(java.util.Locale.ROOT));
        }
        if (fromTs != null) {
            q.append(" and at >= ?").append(params.size() + 1);
            params.add(fromTs);
        }
        if (toTs != null) {
            q.append(" and at <= ?").append(params.size() + 1);
            params.add(toTs);
        }

        QueryParts p = new QueryParts();
        p.jpql = q.toString() + " order by at desc";
        p.params = params;
        return p;
    }

    private Long getCurrentDoctorId() {
        if (identity == null || identity.getPrincipal() == null) return null;
        String amka = identity.getPrincipal().getName();
        Doctor d = Doctor.find("amka", amka).firstResult();
        return d != null ? d.id : null;
    }

    private LocalDateTime parseDateTime(String value, boolean startOfDayIfDateOnly) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        try {
            // ISO LocalDateTime
            return LocalDateTime.parse(v);
        } catch (Exception ignore) {
            // fallback: LocalDate
            try {
                LocalDate d = java.time.LocalDate.parse(v);
                return startOfDayIfDateOnly ? d.atStartOfDay() : d.atTime(23, 59, 59);
            } catch (Exception e2) {
                throw org.medical.error.ApiException.badRequest("Invalid date/time: " + value);
            }
        }
    }

    private static String z(Long v) { return v == null ? "" : v.toString(); }

    private static String escapeCsv(String v) {
        if (v == null) return "";
        boolean needQuotes = v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"");
        String s = v.replace("\"", "\"\"");
        return needQuotes ? '"' + s + '"' : s;
    }
}

