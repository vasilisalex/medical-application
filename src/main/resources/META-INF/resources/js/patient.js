const token = localStorage.getItem('jwt');
if (!token) window.location.href = '/login.html';

document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('jwt');
  window.location.href = '/';
});

const params = new URLSearchParams(location.search);
const amka = params.get('amka');
if (!amka) {
  alert('Λείπει AMKA');
  history.back();
}

const pAmka = document.getElementById('pAmka');
const pName = document.getElementById('pName');
const pDob = document.getElementById('pDob');
const pPhone = document.getElementById('pPhone');
const pEmail = document.getElementById('pEmail');
const pAfm = document.getElementById('pAfm');
const pIdNumber = document.getElementById('pIdNumber');
const pInsurance = document.getElementById('pInsurance');
const pAddress = document.getElementById('pAddress');
const pCreated = document.getElementById('pCreated');
const pUpdated = document.getElementById('pUpdated');
const editPatientBtn = document.getElementById('editPatientBtn');
const newRecordBtn = document.getElementById('newRecordBtn');

const recordsTableBody = document.querySelector('#recordsTable tbody');
const recPrev = document.getElementById('recPrev');
const recNext = document.getElementById('recNext');
const recPageInfo = document.getElementById('recPageInfo');
let recCurrent = [];
let recPage = 1;
const recPageSize = 20;
const recordForm = document.getElementById('recordForm');
const rDate = document.getElementById('rDate');
const rSickness = document.getElementById('rSickness');
const rMedication = document.getElementById('rMedication');
const rExams = document.getElementById('rExams');
const recordMsg = document.getElementById('recordMsg');

async function loadPatient() {
  try {
    const res = await fetch(`/patients/search?amka=${encodeURIComponent(amka)}`, {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Αποτυχία φόρτωσης ασθενή');
  const p = await res.json();
  pAmka.textContent = p.amka || '';
  pName.textContent = `${p.firstName||''} ${p.lastName||''}`.trim();
  pDob.textContent = p.dateOfBirth ? new Date(p.dateOfBirth).toLocaleDateString('el-GR') : '';
  pPhone.textContent = p.phone || '';
  pEmail.textContent = p.email || '';
  pAfm.textContent = p.afm || '';
  pIdNumber.textContent = p.idNumber || '';
  const insuranceMap = { 'public': 'Δημόσια', 'private': 'Ιδιωτική' };
  pInsurance.textContent = insuranceMap[p.insuranceType] || p.insuranceType || '';
  // format address as "Street, City, Postal"
  const street = (p.addressStreet || '').trim();
  const city = (p.addressCity || '').trim();
  const postal = (p.addressPostalCode || '').trim();
  let addr = '';
  if (street) addr += street;
  if (city) addr += (addr ? ', ' : '') + city;
  if (postal) addr += (addr ? ', ' : '') + postal;
  pAddress.textContent = addr;
  pCreated.textContent = p.createdAt ? new Date(p.createdAt).toLocaleString('el-GR') : '';
  pUpdated.textContent = p.updatedAt ? new Date(p.updatedAt).toLocaleString('el-GR') : '';
  if (editPatientBtn) {
    editPatientBtn.href = `/patient-edit.html?amka=${encodeURIComponent(amka)}`;
  }
  if (newRecordBtn) {
    newRecordBtn.href = `/medicalrecord-new.html?amka=${encodeURIComponent(amka)}`;
  }
  } catch (err) {
    console.error(err);
    alert('Σφάλμα φόρτωσης στοιχείων');
  }
}

async function loadRecords() {
  recordsTableBody.innerHTML = '';
  try {
    const res = await fetch(`/medicalrecords/patient/${encodeURIComponent(amka)}`, {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (res.status === 404) {
      // No records: clear table and disable paging controls
      recCurrent = [];
      recPage = 1;
      if (recPageInfo) recPageInfo.textContent = '';
      if (recPrev) recPrev.disabled = true;
      if (recNext) recNext.disabled = true;
      return;
    }
    if (!res.ok) throw new Error('Αποτυχία φόρτωσης ιστορικού');
    const records = await res.json();
    // Sort by date (desc)
    records.sort((a, b) => {
      const da = a.date ? new Date(a.date).getTime() : 0;
      const db = b.date ? new Date(b.date).getTime() : 0;
      return db - da;
    });
    recCurrent = records;
    recPage = 1;
    renderRecordsPaged();
  } catch (err) {
    console.error(err);
  }
}

function renderRecordsPaged() {
  recordsTableBody.innerHTML = '';
  const start = (recPage - 1) * recPageSize;
  const page = recCurrent.slice(start, start + recPageSize);
  page.forEach((r, idx) => {
    const i = start + idx;
    const tr = document.createElement('tr');
    tr.style.cursor = 'pointer';
    tr.addEventListener('click', () => {
      window.location.href = `/medicalrecord.html?amka=${encodeURIComponent(amka)}&id=${encodeURIComponent(r.id)}`;
    });
    tr.innerHTML = `
      <td>${i + 1}</td>
      <td>${r.date ? new Date(r.date).toLocaleDateString('el-GR') : ''}</td>
      <td>${r.sickness || ''}</td>
      <td>${r.medication || ''}</td>
      <td>${r.exams || ''}</td>
      <td>${r.doctor ? (r.doctor.firstName||'') + ' ' + (r.doctor.lastName||'') : ''}</td>
    `;
    recordsTableBody.appendChild(tr);
  });
  const totalPages = Math.max(1, Math.ceil(recCurrent.length / recPageSize));
  if (recPageInfo) recPageInfo.textContent = `${recPage}/${totalPages}`;
  if (recPrev) recPrev.disabled = recPage <= 1;
  if (recNext) recNext.disabled = recPage >= totalPages;
}

if (recPrev) recPrev.addEventListener('click', () => {
  if (recPage > 1) { recPage--; renderRecordsPaged(); }
});
if (recNext) recNext.addEventListener('click', () => {
  const totalPages = Math.max(1, Math.ceil(recCurrent.length / recPageSize));
  if (recPage < totalPages) { recPage++; renderRecordsPaged(); }
});

// Διαγραφή γίνεται από σελίδα λεπτομερειών αν χρειαστεί

if (recordForm) recordForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  recordMsg.textContent = '';
  const payload = {
    date: rDate.value,
    sickness: rSickness.value.trim(),
    medication: rMedication.value.trim(),
    exams: rExams.value.trim(),
    patientAmka: amka
  };
  if (!payload.date || !payload.sickness) {
    recordMsg.textContent = 'Συμπληρώστε ημερομηνία και διάγνωση';
    return;
  }
  try {
    const res = await fetch('/medicalrecords', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify(payload)
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const msg = (Array.isArray(data.errors) && data.errors[0]?.message) || data.error || 'Σφάλμα καταχώρησης';
      recordMsg.textContent = msg;
      return;
    }
    recordForm.reset();
    await loadRecords();
  } catch (err) {
    console.error(err);
    recordMsg.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});

loadPatient().then(loadRecords);
