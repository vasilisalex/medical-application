#medical-app
backend εφαρμογή για την καταγραφή και διαχείριση ιατρικού ιστορικού ασθενών. Χρησιμοποιείται από γιατρούς για να αναζητούν ασθενείς, να βλέπουν και να ενημερώνουν τα ιατρικά τους δεδομένα.

#Περιγραφή
Η εφαρμογή επιτρέπει:
- Εγγραφή και σύνδεση γιατρών (μέσω ΑΜΚΑ και password)
- Καταγραφή ασθενών και των στοιχείων τους
- Εισαγωγή, ανάκτηση, διαγραφή ιατρικών επισκέψεων
- JWT-based authentication και προστασία endpoints
- CRUD λειτουργίες μέσω REST API (δοκιμασμένες σε Postman)

#Τεχνολογίες και Εργαλεία Backend
- Java 21
- Quarkus
- RESTEasy για την υλοποίηση REST API
- Hibernate ORM + Panache για persistence
- PostgreSQL ως σχεσιακή βάση δεδομένων
- JWT με RSA κλειδιά για αυθεντικοποίηση
- DTO Pattern για ασφαλή ανταλλαγή δεδομένων
- Validation για ασφαλή εισαγωγή δεδομένων

#Εργαλεία Ανάπτυξης
- Maven (διαχείριση έργου & εξαρτήσεων)
- Postman (έλεγχος και δοκιμή endpoints)
- GitHub Desktop (version control & συνεργασία)
- VS Code (κύριο IDE)

#Λειτουργίες API
- POST /doctors/register: Εγγραφή νέου γιατρού
- POST /doctors/login: Σύνδεση και λήψη JWT
- POST /patients: Καταχώρηση νέου ασθενή
- GET /patients/{amka}: Αναζήτηση ασθενή με βάση το ΑΜΚΑ
- POST /medical-records: Εισαγωγή νέας επίσκεψης
- GET /medical-records/{amka}: Ιστορικό επισκέψεων ασθενούς
- DELETE /medical-records/{id}: Διαγραφή επίσκεψης
Όλα τα endpoints (εκτός του /login και /register) προστατεύονται μέσω JWT.
