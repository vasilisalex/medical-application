#!/usr/bin/env bash

# Demo: trigger standardized API errors (400/401/403/404/409)
# Requires: running dev server (mvn quarkus:dev) and optional `jq` for nicer output

BASE=${BASE:-http://localhost:${PORT:-8080}}
JSON='Content-Type: application/json'

say() { echo -e "\n=== $* ==="; }

curlj() {
  # Curl JSON with status code separated
  curl -s -H "$JSON" "$@" -w "\nHTTP_STATUS:%{http_code}\n"
}

login() {
  local amka="$1" pw="${2:-Abcdef1!}"
  curl -s -H "$JSON" -X POST "$BASE/doctors/login" \
    -d '{"amka":"'"$amka"'","password":"'"$pw"'"}' \
  | { jq -r '.token // empty' 2>/dev/null || sed -n 's/.*"token"\s*:\s*"\([^"]*\)".*/\1/p'; }
}

say "Login as seeded Doctor A (11111111111)"
TOKEN_A=$(login 11111111111)
if [ -z "$TOKEN_A" ]; then echo "Failed to login as A"; exit 1; fi
echo "TOKEN_A: ${TOKEN_A:0:20}..."

say "Login as seeded Doctor B (22222222222)"
TOKEN_B=$(login 22222222222)
if [ -z "$TOKEN_B" ]; then echo "Failed to login as B"; exit 1; fi
echo "TOKEN_B: ${TOKEN_B:0:20}..."

# 400 Bad Request (business logic): mismatched passwords on registration
say "400 Bad Request: /doctors/register mismatched passwords"
curlj -X POST "$BASE/doctors/register" -d '{
  "amka":"33333333333","firstName":"A","lastName":"B","email":"a3@example.com",
  "password":"Abcdef1!","confirmPassword":"DIFFERENT",
  "specialty":"Cardio","licenseNumber":"L3","medicalAssociation":"MA",
  "phone":"2100000003","officeStreet":"S","officeCity":"C","officePostalCode":"12345" }'

# 400 Validation Error: invalid email
say "400 Validation: /doctors/register invalid email"
curlj -X POST "$BASE/doctors/register" -d '{
  "amka":"44444444444","firstName":"A","lastName":"B","email":"not-an-email",
  "password":"Abcdef1!","confirmPassword":"Abcdef1!",
  "specialty":"Cardio","licenseNumber":"L4","medicalAssociation":"MA",
  "phone":"2100000004","officeStreet":"S","officeCity":"C","officePostalCode":"12345" }'

# 401 Unauthorized: wrong password on login
say "401 Unauthorized: /doctors/login wrong password"
curlj -X POST "$BASE/doctors/login" -d '{"amka":"11111111111","password":"WRONG"}'

# 404 Not Found: patient search that doesn't exist
say "404 Not Found: /patients/search?amka=00000000000"
curl -s -H "Authorization: Bearer $TOKEN_A" "$BASE/patients/search?amka=00000000000" -w "\nHTTP_STATUS:%{http_code}\n"

# 403 Forbidden: delete medical record created by Doctor A while authenticated as Doctor B
say "403 Forbidden: DELETE /medicalrecords/{id} as Doctor B"
REC_ID=$(curl -s -H "Authorization: Bearer $TOKEN_A" "$BASE/medicalrecords/patient/99999999999" | { jq -r '.[0].id // empty' 2>/dev/null || sed -n 's/.*"id"\s*:\s*\([0-9][0-9]*\).*/\1/p'; })
if [ -z "$REC_ID" ]; then echo "Could not obtain seeded medical record id"; else
  curl -s -X DELETE "$BASE/medicalrecords/$REC_ID" -H "Authorization: Bearer $TOKEN_B" -w "\nHTTP_STATUS:%{http_code}\n"
fi

# 409 Conflict: try to change Doctor A's email to Doctor B's email
say "409 Conflict: PUT /doctors/me set email to existing one"
curlj -X PUT "$BASE/doctors/me" -H "Authorization: Bearer $TOKEN_A" -d '{
  "firstName":"Alice","lastName":"Demo","email":"docb@example.com",
  "specialty":"Cardiology","licenseNumber":"LIC-A","medicalAssociation":"Assoc-A",
  "phone":"2100000000","officeStreet":"Alpha 1","officeCity":"Athens","officePostalCode":"12345" }'

say "Done."

