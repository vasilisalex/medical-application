// ελεγχει αν εχουμε token αλλιως παει login
const token = localStorage.getItem('jwt');
if (!token) {
  window.location.href = 'login.html';
}

// κουμπι αποσυνδεσης
document.getElementById('logoutBtn')
  .addEventListener('click', () => {
    localStorage.removeItem('jwt');
    window.location.href = 'login.html';
  });

// οταν η σελιδα φορτωσει τραβαμε δεδομενα
window.addEventListener('DOMContentLoaded', () => {
  fetchPatients();
  fetchRecords();
});

// συναρτηση για ασθενεις
async function fetchPatients() {
  try {
    const res = await fetch('http://localhost:8080/api/patients', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Αδυναμια φορτωσης ασθενων');
    const patients = await res.json();
    const tbody = document.querySelector('#patientsTable tbody');
    patients.forEach((p, i) => {
      const age = calculateAge(p.birthDate);
      tbody.insertAdjacentHTML('beforeend', `
        <tr>
          <td>${i+1}</td>
          <td>${p.firstName} ${p.lastName}</td>
          <td>${age}</td>
          <td>${p.email}</td>
        </tr>
      `);
    });
  } catch (err) {
    console.error(err);
    alert('Σφαλμα φορτωσης ασθενων');
  }
}

// συναρτηση για ιστορικα
async function fetchRecords() {
  try {
    const res = await fetch('http://localhost:8080/api/medical-records', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Αδυναμια φορτωσης ιστορικων');
    const records = await res.json();
    const tbody = document.querySelector('#recordsTable tbody');
    records.forEach((r, i) => {
      const date = new Date(r.date).toLocaleDateString('el-GR');
      tbody.insertAdjacentHTML('beforeend', `
        <tr>
          <td>${i+1}</td>
          <td>${date}</td>
          <td>${r.description}</td>
          <td>${r.patient.firstName} ${r.patient.lastName}</td>
          <td>${r.doctor.firstName} ${r.doctor.lastName}</td>
        </tr>
      `);
    });
  } catch (err) {
    console.error(err);
    alert('Σφαλμα φορτωσης ιστορικων');
  }
}

// βοηθητικη: υπολογιζει ηλικια απο ημερομηνια γεννησης
function calculateAge(birthDateStr) {
  const diff = Date.now() - new Date(birthDateStr).getTime();
  const ageDt = new Date(diff);
  return Math.abs(ageDt.getUTCFullYear() - 1970);
}