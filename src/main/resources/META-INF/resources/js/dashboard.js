// search patient, show records, add new record

const token = localStorage.getItem('jwt');
if (!token) {
  window.location.href = 'login.html';
}

// UI elements
const logoutBtn = document.getElementById('logoutBtn');
const doctorName = document.getElementById('doctorName');
const searchForm = document.getElementById('searchForm');
const searchQuery = document.getElementById('searchQuery');
const sAmka = document.getElementById('sAmka');
const sFirst = document.getElementById('sFirst');
const sLast = document.getElementById('sLast');
const searchMsg = document.getElementById('searchMsg');

const patientCard = document.getElementById('patientCard');
const pAmka = document.getElementById('pAmka');
const pName = document.getElementById('pName');
const pDob = document.getElementById('pDob');
const pAfm = document.getElementById('pAfm');
const pPhone = document.getElementById('pPhone');
const pEmail = document.getElementById('pEmail');

// Αναζήτηση πολλαπλών
const resultsCard = document.getElementById('resultsCard');
const resultsBody = document.getElementById('searchResultsBody');
const prevPageBtn = document.getElementById('prevPage');
const nextPageBtn = document.getElementById('nextPage');
const pageInfo = document.getElementById('pageInfo');
let currentList = [];
let currentPage = 1;
const pageSize = 20;
let currentRecords = [];


// Edit patient form elements (hidden on dashboard) + view button
const editPatientForm = document.getElementById('editPatientForm');
const viewPatientBtn = document.getElementById('viewPatientBtn');
const epPhone = document.getElementById('epPhone');

// Decode doctor info from JWT (robust Base64URL) with fallback to /doctors/me
(async () => {
  try {
    const parts = token.split('.');
    if (parts.length >= 2) {
      const payloadB64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const bin = atob(payloadB64);
      const bytes = new Uint8Array(bin.length);
      for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
      const jsonText = new TextDecoder('utf-8').decode(bytes);
      const payload = JSON.parse(jsonText);
      if (payload && (payload.firstName || payload.lastName)) {
        doctorName.textContent = `${payload.firstName || ''} ${payload.lastName || ''}`.trim();
        return;
      }
    }
  } catch {}
  try {
    const res = await fetch('/doctors/me', { headers: { 'Authorization': 'Bearer ' + token } });
    if (res.ok) {
      const d = await res.json();
      doctorName.textContent = `${d.firstName || ''} ${d.lastName || ''}`.trim();
    }
  } catch {}
})();

logoutBtn.addEventListener('click', () => {
  localStorage.removeItem('jwt');
  window.location.href = '/';
});

let currentPatientAmka = null;

// Ζωντανό φιλτράρισμα τηλεφώνου στο κρυφό edit (αν χρησιμοποιηθεί)
if (epPhone) {
  epPhone.addEventListener('input', () => {
    let v = epPhone.value.replace(/\D/g, '');
    if (v.length > 10) v = v.slice(0, 10);
    epPhone.value = v;
  });
}

searchForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  searchMsg.textContent = '';
  const amka = sAmka ? sAmka.value.trim() : '';
  const first = sFirst ? sFirst.value.trim() : '';
  const last = sLast ? sLast.value.trim() : '';
  if (!amka && !first && !last) { searchMsg.textContent = 'Συμπλήρωσε τουλάχιστον ένα πεδίο'; return; }
  resetPatientUI();
  try {
    const params = new URLSearchParams();
    if (amka) params.set('amka', amka);
    if (first) params.set('first', first);
    if (last) params.set('last', last);
    const res = await fetch(`/patients/search-adv?${params.toString()}`, {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Σφάλμα αναζήτησης ασθενή');
    const list = await res.json();
    if (!Array.isArray(list) || list.length === 0) {
      searchMsg.textContent = 'Δεν βρέθηκαν αποτελέσματα';
      return;
    }
    currentList = list;
    currentPage = 1;
    renderPagedResults();
  } catch (err) {
    console.error(err);
    searchMsg.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});

// AMKA input: μόνο ψηφία μέχρι 11
if (sAmka) sAmka.addEventListener('input', () => {
  let v = sAmka.value.replace(/\D/g, '');
  if (v.length > 11) v = v.slice(0, 11);
  sAmka.value = v;
});

function showPatient(p) {
  pAmka.textContent = p.amka || '';
  pName.textContent = `${p.firstName || ''} ${p.lastName || ''}`.trim();
  pDob.textContent = p.dateOfBirth ? new Date(p.dateOfBirth).toLocaleDateString('el-GR') : '';
  pAfm.textContent = p.afm || '';
  pPhone.textContent = p.phone || '';
  pEmail.textContent = p.email || '';
  patientCard.classList.remove('d-none');

  // Set direct link to patient page
  if (typeof viewPatientBtn !== 'undefined' && viewPatientBtn) {
    viewPatientBtn.href = `/patient.html?amka=${encodeURIComponent(p.amka)}`;
  }
}

function renderPagedResults() {
  resultsBody.innerHTML = '';
  const start = (currentPage - 1) * pageSize;
  const page = currentList.slice(start, start + pageSize);
  page.forEach((p, idx) => {
    const i = start + idx;
    const tr = document.createElement('tr');
    const street = (p.addressStreet || '').trim();
    const city = (p.addressCity || '').trim();
    const postal = (p.addressPostalCode || '').trim();
    let addr = '';
    if (street) addr += street;
    if (city) addr += (addr ? ', ' : '') + city;
    if (postal) addr += (addr ? ', ' : '') + postal;
    tr.innerHTML = `
      <td>${i + 1}</td>
      <td>${p.amka || ''}</td>
      <td>${(p.firstName || '') + ' ' + (p.lastName || '')}</td>
      <td>${addr}</td>
    `;
    tr.style.cursor = 'pointer';
    tr.addEventListener('click', () => {
      window.location.href = `/patient.html?amka=${encodeURIComponent(p.amka)}`;
    });
    resultsBody.appendChild(tr);
  });
  resultsCard.classList.remove('d-none');
  const totalPages = Math.max(1, Math.ceil(currentList.length / pageSize));
  if (pageInfo) pageInfo.textContent = `${currentPage}/${totalPages}`;
  if (prevPageBtn) prevPageBtn.disabled = currentPage <= 1;
  if (nextPageBtn) nextPageBtn.disabled = currentPage >= totalPages;
}

async function loadRecords(amka) {
  recordsTableBody.innerHTML = '';
  recordsCard.classList.add('d-none');
  try {
    const res = await fetch(`/medicalrecords/patient/${encodeURIComponent(amka)}`, {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (res.status === 404) {
      recordsCard.classList.remove('d-none');
      return; // no records yet
    }
    if (!res.ok) throw new Error('Σφάλμα φόρτωσης ιστορικού');
    const records = await res.json();
    currentRecords = records || [];
    currentRecords.forEach((r, i) => {
      const date = r.date ? new Date(r.date).toLocaleDateString('el-GR') : '';
      const doctorName = r.doctor ? `${r.doctor.firstName || ''} ${r.doctor.lastName || ''}`.trim() : '';
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${i + 1}</td>
        <td>${date}</td>
        <td>${r.sickness || ''}</td>
        <td>${r.medication || ''}</td>
        <td>${r.exams || ''}</td>
        <td>${doctorName}</td>
      `;
      recordsTableBody.appendChild(tr);
    });
    recordsCard.classList.remove('d-none');
  } catch (err) {
    console.error(err);
    recordsCard.classList.remove('d-none');
  }
}

// Delegated actions for Edit/Delete on records table
    currentRecords.forEach((r, i) => {
      const date = r.date ? new Date(r.date).toLocaleDateString('el-GR') : '';
      const doctorName = r.doctor ? `${r.doctor.firstName || ''} ${r.doctor.lastName || ''}`.trim() : '';
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${i + 1}</td>
        <td>${date}</td>
        <td>${r.sickness || ''}</td>
        <td>${r.medication || ''}</td>
        <td>${r.exams || ''}</td>
        <td>${doctorName}</td>
      `;
      recordsTableBody.appendChild(tr);
    });

function resetPatientUI() {
  currentPatientAmka = null;
  patientCard.classList.add('d-none');
  if (resultsCard) resultsCard.classList.add('d-none');
  // recordsCard/recordsTable may μην υπάρχουν στο dashboard
  if (typeof recordsCard !== 'undefined' && recordsCard) recordsCard.classList.add('d-none');
  if (typeof recordsTableBody !== 'undefined' && recordsTableBody) recordsTableBody.innerHTML = '';
}

if (prevPageBtn) prevPageBtn.addEventListener('click', () => {
  if (currentPage > 1) { currentPage--; renderPagedResults(); }
});
if (nextPageBtn) nextPageBtn.addEventListener('click', () => {
  const totalPages = Math.max(1, Math.ceil(currentList.length / pageSize));
  if (currentPage < totalPages) { currentPage++; renderPagedResults(); }
});
