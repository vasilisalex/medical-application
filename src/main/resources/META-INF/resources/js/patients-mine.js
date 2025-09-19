const token = localStorage.getItem('jwt');
if (!token) window.location.href = '/login.html';

document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('jwt');
  window.location.href = '/';
});

const tbody = document.querySelector('#patientsTable tbody');
const msg = document.getElementById('msg');
const pmPrev = document.getElementById('pmPrev');
const pmNext = document.getElementById('pmNext');
const pmPageInfo = document.getElementById('pmPageInfo');
let pmList = [];
let pmPage = 1;
const pmPageSize = 20;

async function loadPatients() {
  try {
    const res = await fetch('/patients/mine', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Αποτυχία φόρτωσης');
    pmList = await res.json();
    pmPage = 1;
    renderPm();
  } catch (err) {
    console.error(err);
    msg.textContent = 'Σφάλμα φόρτωσης λίστας';
  }
}

loadPatients();

function renderPm() {
  tbody.innerHTML = '';
  const start = (pmPage - 1) * pmPageSize;
  const page = pmList.slice(start, start + pmPageSize);
  page.forEach((p, idx) => {
    const i = start + idx;
    const street = (p.addressStreet || '').trim();
    const city = (p.addressCity || '').trim();
    const postal = (p.addressPostalCode || '').trim();
    let addr = '';
    if (street) addr += street;
    if (city) addr += (addr ? ', ' : '') + city;
    if (postal) addr += (addr ? ', ' : '') + postal;
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${i+1}</td>
      <td>${p.amka}</td>
      <td>${(p.firstName||'') + ' ' + (p.lastName||'')}</td>
      <td>${addr}</td>
    `;
    tr.style.cursor = 'pointer';
    tr.addEventListener('click', () => {
      window.location.href = `/patient.html?amka=${encodeURIComponent(p.amka)}`;
    });
    tbody.appendChild(tr);
  });
  const totalPages = Math.max(1, Math.ceil(pmList.length / pmPageSize));
  if (pmPageInfo) pmPageInfo.textContent = pmList.length ? `${pmPage}/${totalPages}` : '';
  if (pmPrev) pmPrev.disabled = pmPage <= 1 || pmList.length === 0;
  if (pmNext) pmNext.disabled = pmPage >= totalPages || pmList.length === 0;
}

if (pmPrev) pmPrev.addEventListener('click', () => {
  if (pmPage > 1) { pmPage--; renderPm(); }
});
if (pmNext) pmNext.addEventListener('click', () => {
  const totalPages = Math.max(1, Math.ceil(pmList.length / pmPageSize));
  if (pmPage < totalPages) { pmPage++; renderPm(); }
});
