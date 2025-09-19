const token = localStorage.getItem('jwt');
if (!token) window.location.href = '/login.html';

document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('jwt');
  window.location.href = '/';
});

const params = new URLSearchParams(location.search);
const amka = params.get('amka');
const id = params.get('id');
const backBtn = document.getElementById('backToPatient');
backBtn.href = `/patient.html?amka=${encodeURIComponent(amka)}`;

// patient fields
const pAmka = document.getElementById('pAmka');
const pName = document.getElementById('pName');
const pDob = document.getElementById('pDob');
const pPhone = document.getElementById('pPhone');
const pEmail = document.getElementById('pEmail');

// record fields
const form = document.getElementById('editForm');
const dateEl = document.getElementById('date');
const sicknessEl = document.getElementById('sickness');
const medicationEl = document.getElementById('medication');
const examsEl = document.getElementById('exams');
const visitTypeEl = document.getElementById('visitType');
const facilityEl = document.getElementById('facility');
const symptomsEl = document.getElementById('symptoms');
const diagnosisCodeEl = document.getElementById('diagnosisCode');
const dosageEl = document.getElementById('dosage');
const followUpDateEl = document.getElementById('followUpDate');
const notesEl = document.getElementById('notes');
const saveBtn = document.getElementById('saveBtn');
const msg = document.getElementById('msg');
const deleteRecBtn = document.getElementById('deleteRecBtn');
const recDoctor = document.getElementById('recDoctor');
const recDoctorSpec = document.getElementById('recDoctorSpec');
const recCreated = document.getElementById('recCreated');
const recUpdated = document.getElementById('recUpdated');
const pDobField = document.getElementById('pDobField');
const pPhoneField = document.getElementById('pPhoneField');
const pEmailField = document.getElementById('pEmailField');

async function loadPatient() {
  const res = await fetch(`/patients/search?amka=${encodeURIComponent(amka)}`, {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) return;
  const p = await res.json();
  pAmka.textContent = p.amka || '';
  pName.textContent = `${p.firstName||''} ${p.lastName||''}`.trim();
  if (pDob) pDob.textContent = p.dateOfBirth ? new Date(p.dateOfBirth).toLocaleDateString('el-GR') : '';
  if (pPhone) pPhone.textContent = p.phone || '';
  if (pEmail) pEmail.textContent = p.email || '';
}

async function loadRecord() {
  const res = await fetch(`/medicalrecords/${encodeURIComponent(id)}`, {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) return;
  const r = await res.json();
  dateEl.value = r.date || '';
  sicknessEl.value = r.sickness || '';
  medicationEl.value = r.medication || '';
  examsEl.value = r.exams || '';
  visitTypeEl.value = r.visitType || '';
  facilityEl.value = r.facility || '';
  symptomsEl.value = r.symptoms || '';
  diagnosisCodeEl.value = r.diagnosisCode || '';
  dosageEl.value = r.dosage || '';
  followUpDateEl.value = r.followUpDate || '';
  notesEl.value = r.notes || '';

  // Show doctor info
  const docName = r.doctor ? `${r.doctor.firstName||''} ${r.doctor.lastName||''}`.trim() : '';
  if (recDoctor) recDoctor.textContent = docName;
  if (recDoctorSpec) recDoctorSpec.textContent = r.doctorSpecialty || '';
  if (recCreated) recCreated.textContent = r.createdAt ? new Date(r.createdAt).toLocaleString('el-GR') : '';
  if (recUpdated) recUpdated.textContent = r.updatedAt ? new Date(r.updatedAt).toLocaleString('el-GR') : '';

  // view-only if τρέχων γιατρός δεν είναι ο δημιουργός
  let myAmka = null;
  try {
    const parts = token.split('.');
    if (parts.length >= 2) {
      const payloadB64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const bin = atob(payloadB64);
      const bytes = new Uint8Array(bin.length);
      for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
      const payload = JSON.parse(new TextDecoder('utf-8').decode(bytes));
      myAmka = payload && payload.sub;
    }
  } catch {}
  const creatorAmka = r.doctor && r.doctor.amka ? r.doctor.amka : null;
  if (!myAmka || !creatorAmka || myAmka !== creatorAmka) {
    [dateEl,sicknessEl,medicationEl,examsEl,visitTypeEl,facilityEl,symptomsEl,diagnosisCodeEl,dosageEl,followUpDateEl,notesEl]
      .forEach(el => el && el.setAttribute('disabled','disabled'));
    if (saveBtn) saveBtn.style.display = 'none';
    // Compact patient info like on new record (only AMKA + Ονοματεπώνυμο)
    if (pDobField) pDobField.style.display = 'none';
    if (pPhoneField) pPhoneField.style.display = 'none';
    if (pEmailField) pEmailField.style.display = 'none';
    if (deleteRecBtn) deleteRecBtn.style.display = 'none';
  } else {
    if (deleteRecBtn) deleteRecBtn.style.display = 'inline-block';
  }
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  msg.textContent = '';
  const payload = {
    date: dateEl.value,
    sickness: sicknessEl.value.trim(),
    medication: medicationEl.value.trim(),
    exams: examsEl.value.trim(),
    patientAmka: amka
  };
  if (!payload.date || !payload.sickness) {
    msg.textContent = 'Συμπληρώστε ημερομηνία και διάγνωση';
    return;
  }
  try {
    const res = await fetch(`/medicalrecords/${encodeURIComponent(id)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify(payload)
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const m = (Array.isArray(data.errors) && data.errors[0]?.message) || data.error || 'Αποτυχία ενημέρωσης';
      msg.textContent = m;
      return;
    }
    msg.style.color = 'green';
    msg.textContent = 'Αποθηκεύτηκε';
    setTimeout(() => { window.location.href = `/patient.html?amka=${encodeURIComponent(amka)}`; }, 800);
  } catch (err) {
    console.error(err);
    msg.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});

// Διαγραφή ιατρικής εγγραφής (μόνο για δημιουργό — κουμπί κρύβεται αν δεν είναι δημιουργός)
if (deleteRecBtn) {
  deleteRecBtn.addEventListener('click', async () => {
    const ok = confirm('Επιβεβαίωση διαγραφής της ιατρικής εγγραφής; Η ενέργεια δεν αναιρείται.');
    if (!ok) return;
    msg.textContent = '';
    try {
      const res = await fetch(`/medicalrecords/${encodeURIComponent(id)}`, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token }
      });
      if (!res.ok) {
        msg.textContent = 'Αποτυχία διαγραφής';
        return;
      }
      window.location.href = `/patient.html?amka=${encodeURIComponent(amka)}`;
    } catch (err) {
      console.error(err);
      msg.textContent = 'Σφάλμα σύνδεσης με τον server';
    }
  });
}

loadPatient();
loadRecord();
