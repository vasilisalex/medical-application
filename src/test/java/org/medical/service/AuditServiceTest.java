package org.medical.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.medical.model.AuditLog;
import org.medical.model.Doctor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuditServiceTest {

    @Inject
    AuditService auditService;

    @Test
    @TestSecurity(user = "11111111111", roles = {"doctor"})
    void log_writes_expected_fields() {
        // given
        String patientAmka = "99999999999";
        Long recordId = 12345L;
        String action = "READ";

        Doctor d = Doctor.find("amka", "11111111111").firstResult();
        assertNotNull(d, "Seeded doctor not found");

        // when
        auditService.log(action, patientAmka, recordId);

        // then
        List<AuditLog> rows = AuditLog.list("doctorId = ?1 and patientAmka = ?2 and recordId = ?3 and action = ?4",
                d.id, patientAmka, recordId, action);
        assertFalse(rows.isEmpty(), "Audit row should exist");

        AuditLog al = rows.get(0);
        assertEquals(d.id, al.doctorId);
        assertEquals(patientAmka, al.patientAmka);
        assertEquals(recordId, al.recordId);
        assertEquals(action, al.action);
        assertNotNull(al.at);
    }
}

