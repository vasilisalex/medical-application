// Βοηθητικα στοιχεία UI
const form = document.getElementById('register-form');
const amkaInput = document.getElementById('amka');
const amkaHelp = document.getElementById('amkaHelp');
const messageEl = document.getElementById('message');
const pwInput = document.getElementById('password');
const pwHelp = document.getElementById('passwordHelp');
const pwcInput = document.getElementById('confirmPassword');
const pwcHelp = document.getElementById('confirmPasswordHelp');
const officePostalInput = document.getElementById('officePostalCode');
const phoneInput = document.getElementById('phone');

// AMKA: καθάρισμα κατά την πληκτρολόγηση, εμφάνιση μηνύματος μόνο σε blur/εστίαση αλλού/submit
function sanitizeAmka() {
  let v = amkaInput.value.replace(/\D/g, '');
  if (v.length > 11) v = v.slice(0, 11);
  amkaInput.value = v;
  return v;
}

function updateAmkaValidity(showMsg) {
  const raw = amkaInput.value;
  const v = sanitizeAmka();
  const ok = v === '' || /^\d{11}$/.test(v);
  if (!ok && showMsg) {
    const hadNonDigits = raw !== v; // αν αφαιρέθηκαν μη-ψηφία κατά το sanitize
    const msg = hadNonDigits
      ? 'Επιτρέπονται μόνο αριθμητικοί χαρακτήρες'
      : 'Το AMKA πρέπει να είναι ακριβώς 11 ψηφία';
    amkaHelp.textContent = msg;
    amkaInput.setCustomValidity(msg);
  } else {
    amkaHelp.textContent = '';
    amkaInput.setCustomValidity('');
  }
  return ok;
}

// Κατά την πληκτρολόγηση δεν δείχνουμε μήνυμα
amkaInput.addEventListener('input', () => updateAmkaValidity(false));
// Όταν φύγει το focus από το AMKA, δείχνουμε μήνυμα αν δεν είναι έγκυρο
amkaInput.addEventListener('blur', () => updateAmkaValidity(true));

// Password complexity helpers (ASCII-only, no spaces)
function passwordStatus(pw) {
  const missing = [];
  const invalidChars = /[^\x21-\x7E]/.test(pw); // allow only printable ASCII except space
  if (!pw || pw.length < 8) missing.push('τουλάχιστον 8 χαρακτήρες');
  if (!/[a-z]/.test(pw)) missing.push('πεζά');
  if (!/[A-Z]/.test(pw)) missing.push('κεφαλαία');
  if (!/[0-9]/.test(pw)) missing.push('αριθμό');
  if (!/[^A-Za-z0-9]/.test(pw)) missing.push('ειδικός χαρακτήρας');
  return { missing, invalidChars };
}

function updatePasswordValidity(showMsg) {
  const pw = pwInput.value;
  const { missing, invalidChars } = passwordStatus(pw);
  const ok = !invalidChars && missing.length === 0;
  if (!ok && showMsg) {
    const parts = [];
    if (invalidChars) parts.push('Περιέχει μη επιτρεπτούς χαρακτήρες (επιτρέπονται μόνο λατινικοί χαρακτήρες ASCII, χωρίς κενά).');
    if (missing.length) parts.push('Λείπουν: ' + missing.join(', ') + '.');
    const msg = 'Μη έγκυρος κωδικός. ' + parts.join(' ');
    pwHelp.textContent = msg;
    pwInput.setCustomValidity(msg);
  } else {
    pwHelp.textContent = '';
    pwInput.setCustomValidity('');
  }
  return ok;
}

function updateConfirmValidity(showMsg) {
  const pw = pwInput.value;
  const pc = pwcInput.value;
  const ok = !pc || pw === pc; // μην δείχνουμε νωρίς πριν πληκτρολογήσει
  if (!ok && showMsg) {
    const msg = 'Η επιβεβαίωση κωδικού δεν ταιριάζει.';
    pwcHelp.textContent = msg;
    pwcInput.setCustomValidity(msg);
  } else {
    pwcHelp.textContent = '';
    pwcInput.setCustomValidity('');
  }
  return ok;
}

pwInput.addEventListener('input', () => updatePasswordValidity(false));
pwInput.addEventListener('blur', () => updatePasswordValidity(true));
pwcInput.addEventListener('input', () => updateConfirmValidity(false));
pwcInput.addEventListener('blur', () => updateConfirmValidity(true));

// Ζωντανός έλεγχος ΤΚ: μόνο ψηφία και μέγιστο 5
if (officePostalInput) {
  officePostalInput.addEventListener('input', () => {
    let v = officePostalInput.value.replace(/\D/g, '');
    if (v.length > 5) v = v.slice(0, 5);
    officePostalInput.value = v;
    if (v && !/^\d{5}$/.test(v)) {
      officePostalInput.setCustomValidity('Ακριβώς 5 ψηφία');
    } else {
      officePostalInput.setCustomValidity('');
    }
  });
}

// Τηλέφωνο: μόνο ψηφία και max 10, με μήνυμα σε μη-έγκυρο submit
if (phoneInput) {
  phoneInput.addEventListener('input', () => {
    let v = phoneInput.value.replace(/\D/g, '');
    if (v.length > 10) v = v.slice(0, 10);
    phoneInput.value = v;
  });
}

// χειριζομαστε την υποβολη της φορμας εγγραφης
form.addEventListener('submit', async (e) => {
  e.preventDefault();
  messageEl.textContent = '';

  // Τοπικες τιμές
  const amka = amkaInput.value.trim();
  const firstName = document.getElementById('firstName').value.trim();
  const lastName  = document.getElementById('lastName').value.trim();
  const email     = document.getElementById('email').value.trim();
  const password  = document.getElementById('password').value;
  const confirmPw = document.getElementById('confirmPassword').value;
  const specialty = document.getElementById('specialty') ? document.getElementById('specialty').value.trim() : '';
  const licenseNumber = document.getElementById('licenseNumber') ? document.getElementById('licenseNumber').value.trim() : '';
  const medicalAssociation = document.getElementById('medicalAssociation') ? document.getElementById('medicalAssociation').value.trim() : '';
  const phone = document.getElementById('phone') ? document.getElementById('phone').value.trim() : '';
  const officeStreet = document.getElementById('officeStreet') ? document.getElementById('officeStreet').value.trim() : '';
  const officeCity = document.getElementById('officeCity') ? document.getElementById('officeCity').value.trim() : '';
  const officePostalCode = officePostalInput ? officePostalInput.value.trim() : '';

  // Ελεγχος AMKA — εμφάνισε μήνυμα αν είναι μη έγκυρο
  if (!updateAmkaValidity(true)) {
    amkaInput.reportValidity();
    return;
  }

  // Βασικες απαιτήσεις
  if (!amka || !firstName || !lastName || !email || !password || !confirmPw || !specialty || !licenseNumber || !medicalAssociation || !phone || !officeStreet || !officeCity || !officePostalCode) {
    messageEl.style.color = '#d00';
    messageEl.textContent = 'Συμπληρώστε όλα τα πεδία';
    return;
  }

  if (!/^\d{5}$/.test(officePostalCode)) {
    messageEl.style.color = '#d00';
    messageEl.textContent = 'ΤΚ: ακριβώς 5 ψηφία';
    officePostalInput?.reportValidity();
    return;
  }

  if (!/^\d{10}$/.test(phone)) {
    messageEl.style.color = '#d00';
    messageEl.textContent = 'Τηλέφωνο: ακριβώς 10 ψηφία';
    phoneInput?.reportValidity();
    return;
  }

  // Έλεγχος πολυπλοκότητας password με μήνυμα κάτω από το πεδίο
  if (!updatePasswordValidity(true)) { pwInput.reportValidity(); return; }
  // Έλεγχος ταύτισης επιβεβαίωσης
  if (!updateConfirmValidity(true)) { pwcInput.reportValidity(); return; }

  try {
    const res = await fetch('/doctors/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        amka, firstName, lastName, email, password,
        confirmPassword: confirmPw,
        specialty, licenseNumber, medicalAssociation, phone,
        officeStreet, officeCity, officePostalCode
      })
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      const firstValidation = Array.isArray(data.errors) && data.errors.length > 0
        ? data.errors[0].message
        : null;
      const msg = firstValidation || data.error || data.message || 'Σφάλμα στην εγγραφή';
      messageEl.style.color = '#d00';
      messageEl.textContent = msg;
      return;
    }

    messageEl.style.color = 'green';
    messageEl.textContent = 'Επιτυχής εγγραφή! Συνδεθείτε τώρα.';
    setTimeout(() => (window.location.href = 'login.html'), 1200);

  } catch (err) {
    console.error(err);
    messageEl.style.color = '#d00';
    messageEl.textContent = 'Σφάλμα σύνδεσης με τον server';
  }
});
