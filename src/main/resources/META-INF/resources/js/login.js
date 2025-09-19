const loginForm = document.getElementById('loginForm');
const loginAmkaInput = document.getElementById('amka');
const loginAmkaHelp = document.getElementById('loginAmkaHelp');
const passwordInput = document.getElementById('password');

function sanitizeAmka() {
  let v = loginAmkaInput.value.replace(/\D/g, '');
  if (v.length > 11) v = v.slice(0, 11);
  loginAmkaInput.value = v;
  return v;
}

function updateAmkaValidity(showMsg) {
  const v = sanitizeAmka();
  const ok = v === '' || /^\d{11}$/.test(v);
  if (!ok && showMsg) {
    const msg = 'Απαιτούνται ακριβώς 11 ψηφία';
    loginAmkaHelp.textContent = msg;
    loginAmkaInput.setCustomValidity(msg);
  } else {
    loginAmkaHelp.textContent = '';
    loginAmkaInput.setCustomValidity('');
  }
  return ok;
}

// Κατά την πληκτρολόγηση: φίλτρο, χωρίς μήνυμα
loginAmkaInput.addEventListener('input', () => updateAmkaValidity(false));
// Όταν φύγει το focus από το AMKA: δείξε μήνυμα αν δεν είναι έγκυρο
loginAmkaInput.addEventListener('blur', () => updateAmkaValidity(true));
// Όταν πάει να συμπληρώσει κωδικό: έλεγξε και εμφάνισε μήνυμα αν χρειάζεται
passwordInput.addEventListener('focus', () => updateAmkaValidity(true));

loginForm.addEventListener('submit', async function(e) {
  e.preventDefault();

  const amka = loginAmkaInput.value.trim();
  const password = passwordInput.value;
  const messageEl = document.getElementById('loginMessage');
  messageEl.style.display = 'none';

  const ok = updateAmkaValidity(true);
  if (!ok) { loginAmkaInput.reportValidity(); return; }

  if (!amka || !password) {
    messageEl.textContent = 'Συμπλήρωσε όλα τα πεδία';
    messageEl.style.display = 'block';
    return;
  }

  try {
    const resp = await fetch('/doctors/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amka, password })
    });
    const result = await resp.json();

    if (resp.ok) {
      // store JWT and go to dashboard
      if (result && result.token) {
        localStorage.setItem('jwt', result.token);
      }
      window.location.href = '/dashboard.html';
    } else {
      const errs = Array.isArray(result.errors) && result.errors.length > 0 ? result.errors[0].message : null;
      messageEl.textContent = errs || result.error || 'Λάθος AMKA ή κωδικός!';
      messageEl.style.display = 'block';
    }
  } catch (err) {
    messageEl.textContent = 'Σφάλμα σύνδεσης με τον server';
    messageEl.style.display = 'block';
  }
});
