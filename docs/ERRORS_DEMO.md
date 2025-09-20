# Errors Demo

This project standardizes API errors via ExceptionMappers. Use this guide to run the app and trigger representative 400/401/403/404/409 responses.

## 1) Run in Dev Mode

```
mvn quarkus:dev
```

The app seeds demo data in dev/test profiles:

- Doctor A: AMKA `11111111111`, email `doca@example.com`, password `Abcdef1!`
- Doctor B: AMKA `22222222222`, email `docb@example.com`, password `Abcdef1!`
- Patient P: AMKA `99999999999` (with one medical record created by Doctor A)

## 2) One‑liners with curl

See `scripts/curl-errors.sh` for an automated run. Or copy individual commands from that script. Example to get a token:

```
curl -s -H 'Content-Type: application/json' \
  -X POST http://localhost:8080/doctors/login \
  -d '{"amka":"11111111111","password":"Abcdef1!"}'
```

## 3) Automated Demo Script

```
chmod +x scripts/curl-errors.sh
./scripts/curl-errors.sh
```

The script logs in as seeded doctors and issues requests that return:

- 400 bad_request, validation_error
- 401 unauthorized
- 403 forbidden
- 404 not_found
- 409 conflict

Each response follows the uniform JSON envelope, for example:

```
{ "error": "conflict", "message": "email already in use", "path": "/doctors/me" }
```

