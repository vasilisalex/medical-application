const token = localStorage.getItem('jwt');
if (!token) window.location.href = '/login.html';

document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('jwt');
  window.location.href = '/';
});

const pfAmka = document.getElementById('pfAmka');
const pfEmail = document.getElementById('pfEmail');
const pfFirst = document.getElementById('pfFirst');
const pfLast = document.getElementById('pfLast');
const pfSpecialty = document.getElementById('pfSpecialty');
const pfLicense = document.getElementById('pfLicense');
const pfAssociation = document.getElementById('pfAssociation');
const pfPhone = document.getElementById('pfPhone');
const pfStreet = document.getElementById('pfStreet');
const pfCity = document.getElementById('pfCity');
const pfPostal = document.getElementById('pfPostal');
const profileMsg = document.getElementById('profileMsg');

// έλεγχος ΤΚ (5 ψηφία)
pfPostal.addEventListener('input', () => {
  let v = pfPostal.value.replace(/\D/g, '');
  if (v.length > 5) v = v.slice(0, 5);
  pfPostal.value = v;
  if (v && !/^\d{5}$/.test(v)) {
    pfPostal.setCustomValidity('Ακριβώς 5 ψηφία');
  } else {
    pfPostal.setCustomValidity('');
  }
});

// Ζωντανός έλεγχος τηλεφώνου: μόνο ψηφία και max 10
pfPhone.addEventListener('input', () => {
  let v = pfPhone.value.replace(/\D/g, '');
  if (v.length > 10) v = v.slice(0, 10);
  pfPhone.value = v;
  if (v && !/^\d{10}$/.test(v)) {
    pfPhone.setCustomValidity('Ακριβώς 10 ψηφία');
  } else {
    pfPhone.setCustomValidity('');
  }
});

async function loadProfile() {
  profileMsg.textContent = '';
  try {
    const res = await fetch('/doctors/me', { headers: { 'Authorization': 'Bearer ' + token } });
    if (!res.ok) throw new Error('Αποτυχία φόρτωσης');
    const d = await res.json();
    pfAmka.value = d.amka || '';
    pfEmail.value = d.email || '';
    pfFirst.value = d.firstName || '';
    pfLast.value = d.lastName || '';
    pfSpecialty.value = d.specialty || '';
    pfLicense.value = d.licenseNumber || '';
    pfAssociation.value = d.medicalAssociation || '';
    pfPhone.value = d.phone || '';
    pfStreet.value = d.officeStreet || '';
    pfCity.value = d.officeCity || '';
    pfPostal.value = d.officePostalCode || '';
  } catch (err) {
    console.error(err);
    profileMsg.textContent = 'Σφάλμα φόρτωσης προφίλ';
  }
}

document.getElementById('profileForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  profileMsg.textContent = '';
  const payload = {
    email: pfEmail.value.trim(),
    firstName: pfFirst.value.trim(),
    lastName: pfLast.value.trim(),
    specialty: pfSpecialty.value.trim(),
    licenseNumber: pfLicense.value.trim(),
    medicalAssociation: pfAssociation.value.trim(),
    phone: pfPhone.value.trim(),
    officeStreet: pfStreet.value.trim(),
    officeCity: pfCity.value.trim(),
    officePostalCode: pfPostal.value.trim()
  };
  if (!/^\d{10}$/.test(pfPhone.value.trim())) {
    profileMsg.textContent = 'Τηλέφωνο: ακριβώς 10 ψηφία';
    pfPhone.reportValidity();
    return;
  }
  if (!/^\d{5}$/.test(pfPostal.value.trim())) {
    profileMsg.textContent = 'ΤΚ: ακριβώς 5 ψηφία';
    pfPostal.reportValidity();
    return;
  }
  try {
    const res = await fetch('/doctors/me', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify(payload)
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const msg = (Array.isArray(data.errors) && data.errors[0]?.message) || data.error || 'Αποτυχία ενημέρωσης';
      profileMsg.textContent = msg;
      return;
    }
    profileMsg.style.color = 'green';
    profileMsg.textContent = 'Αποθηκεύτηκε';
    setTimeout(() => { profileMsg.textContent = ''; profileMsg.style.color = ''; }, 1500);
  } catch (err) {
    console.error(err);
    profileMsg.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});

const pwdMsg = document.getElementById('pwdMsg');
const newPwdInput = document.getElementById('newPwd');
const newPwdHelp = document.getElementById('newPwdHelp');
const confPwdInput = document.getElementById('confPwd');
const confPwdHelp = document.getElementById('confPwdHelp');

function passwordStatus(pw) {
  const missing = [];
  const invalidChars = /[^\x21-\x7E]/.test(pw); // only printable ASCII, no spaces
  if (!pw || pw.length < 8) missing.push('τουλάχιστον 8 χαρακτήρες');
  if (!/[a-z]/.test(pw)) missing.push('πεζά');
  if (!/[A-Z]/.test(pw)) missing.push('κεφαλαία');
  if (!/[0-9]/.test(pw)) missing.push('αριθμό');
  if (!/[^A-Za-z0-9]/.test(pw)) missing.push('ειδικός χαρακτήρας');
  return { missing, invalidChars };
}

function updateNewPwdValidity(showMsg) {
  const pw = newPwdInput.value;
  const { missing, invalidChars } = passwordStatus(pw);
  const ok = !invalidChars && missing.length === 0;
  if (!ok && showMsg) {
    const parts = [];
    if (invalidChars) parts.push('Περιέχει μη επιτρεπτούς χαρακτήρες (επιτρέπονται μόνο λατινικοί χαρακτήρες ASCII, χωρίς κενά).');
    if (missing.length) parts.push('Λείπουν: ' + missing.join(', ') + '.');
    const msg = 'Μη έγκυρος κωδικός. ' + parts.join(' ');
    newPwdHelp.textContent = msg;
    newPwdInput.setCustomValidity(msg);
  } else {
    newPwdHelp.textContent = '';
    newPwdInput.setCustomValidity('');
  }
  return ok;
}

function updateConfPwdValidity(showMsg) {
  const pw = newPwdInput.value;
  const pc = confPwdInput.value;
  const ok = !pc || pw === pc;
  if (!ok && showMsg) {
    const msg = 'Η επιβεβαίωση κωδικού δεν ταιριάζει.';
    confPwdHelp.textContent = msg;
    confPwdInput.setCustomValidity(msg);
  } else {
    confPwdHelp.textContent = '';
    confPwdInput.setCustomValidity('');
  }
  return ok;
}

newPwdInput.addEventListener('input', () => updateNewPwdValidity(false));
newPwdInput.addEventListener('blur', () => updateNewPwdValidity(true));
confPwdInput.addEventListener('input', () => updateConfPwdValidity(false));
confPwdInput.addEventListener('blur', () => updateConfPwdValidity(true));
document.getElementById('passwordForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  pwdMsg.textContent = '';
  if (!e.target.checkValidity()) { e.target.reportValidity(); return; }
  if (!updateNewPwdValidity(true)) { newPwdInput.reportValidity(); return; }
  if (!updateConfPwdValidity(true)) { confPwdInput.reportValidity(); return; }
  const payload = {
    currentPassword: document.getElementById('curPwd').value,
    newPassword: document.getElementById('newPwd').value,
    confirmPassword: document.getElementById('confPwd').value
  };
  try {
    const res = await fetch('/doctors/me/password', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      const msg = (Array.isArray(data.errors) && data.errors[0]?.message) || data.error || 'Αποτυχία αλλαγής κωδικού';
      pwdMsg.textContent = msg;
      return;
    }
    pwdMsg.style.color = 'green';
    pwdMsg.textContent = 'Ο κωδικός άλλαξε';
    e.target.reset();
    setTimeout(() => { pwdMsg.textContent = ''; pwdMsg.style.color = ''; }, 1500);
  } catch (err) {
    console.error(err);
    pwdMsg.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});

loadProfile();
