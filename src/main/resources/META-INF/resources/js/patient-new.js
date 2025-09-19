const token = localStorage.getItem('jwt');
if (!token) window.location.href = '/login.html';

document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('jwt');
  window.location.href = '/';
});

const form = document.getElementById('newPatientForm');
const msg = document.getElementById('msg');
const afterCreate = document.getElementById('afterCreate');
const toPatientBtn = document.getElementById('toPatientBtn');
const amkaInput = document.getElementById('amka');
const postalInput = document.getElementById('postal');
const phoneInput = document.getElementById('phone');

// μόνο ψηφία και max 11
amkaInput.addEventListener('input', () => {
  let v = amkaInput.value.replace(/\D/g, '');
  if (v.length > 11) v = v.slice(0, 11);
  amkaInput.value = v;
});

// μόνο ψηφία και max 5
postalInput.addEventListener('input', () => {
  let v = postalInput.value.replace(/\D/g, '');
  if (v.length > 5) v = v.slice(0, 5);
  postalInput.value = v;
});

// μόνο ψηφία και max 10 για τηλέφωνο
phoneInput.addEventListener('input', () => {
  let v = phoneInput.value.replace(/\D/g, '');
  if (v.length > 10) v = v.slice(0, 10);
  phoneInput.value = v;
});

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  msg.textContent = '';
  const payload = {
    amka: document.getElementById('amka').value.trim(),
    firstName: document.getElementById('first').value.trim(),
    lastName: document.getElementById('last').value.trim(),
    dateOfBirth: document.getElementById('dob').value,
    phone: document.getElementById('phone').value.trim(),
    email: document.getElementById('email').value.trim(),
    afm: document.getElementById('afm').value.trim(),
    idNumber: document.getElementById('idNumber').value.trim(),
    insuranceType: document.getElementById('insuranceType').value,
    addressStreet: document.getElementById('street').value.trim(),
    addressCity: document.getElementById('city').value.trim(),
    addressPostalCode: document.getElementById('postal').value.trim()
  };
  if (payload.phone && !/^\d{10}$/.test(payload.phone)) {
    msg.textContent = 'Τηλέφωνο: αν το συμπληρώσετε, πρέπει να είναι 10 ψηφία';
    return;
  }
  if (payload.addressPostalCode && !/^\d{5}$/.test(payload.addressPostalCode)) {
    msg.textContent = 'ΤΚ: αν το συμπληρώσετε, πρέπει να είναι 5 ψηφία';
    return;
  }
  if (!/^\d{11}$/.test(payload.amka)) {
    msg.textContent = 'AMKA: ακριβώς 11 ψηφία';
    return;
  }
  try {
    const res = await fetch('/patients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify(payload)
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const err = (Array.isArray(data.errors) && data.errors[0]?.message) || data.error || 'Αποτυχία καταχώρησης';
      msg.textContent = err;
      return;
    }
    afterCreate.classList.remove('d-none');
    toPatientBtn.href = `/patient.html?amka=${encodeURIComponent(data.amka)}`;
    form.reset();
  } catch (err) {
    console.error(err);
    msg.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});
