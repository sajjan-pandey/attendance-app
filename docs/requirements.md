# Multi-Tenant Face Recognition Attendance System — POC Requirements

## Objective

Build a scalable, secure, configurable attendance platform for multiple tenants and
projects. Attendance rules must be configured per project instead of hard-coded.

## Tenant hierarchy

```text
Tenant → Project → Users → Attendance Policy → Locations/Camps
                                  ↓                  ↓
                         Attendance Records ← Daily Assignments
```

Every tenant-owned record contains `tenant_id`. Backend authorization must derive
tenant/project context from the authenticated identity; frontend filtering is never a
security boundary.

## Policies

- `FIXED_LOCATION`: all project users use one configured location.
- `ASSIGNED_LOCATION`: user must have an active assignment for the current date/time,
  then the server validates GPS distance against that assigned location.
- `FLEXIBLE_LOCATION`: no fixed location restriction; store attendance type such as
  `OFFICE`, `WFH`, `CLIENT_SITE`, or `FIELD` according to policy.

Default location radius is 50 metres and is configurable per location.

## MMU camps and assignments

The system supports 100+ camps per day. A camp has a schedule date, operating time,
coordinates, radius, and status. User-camp assignments are date-based and can change
daily. Bulk assignment and CSV/Excel import must validate unknown users, unknown
camps, duplicates, time ranges, and return successful/failed row counts.

## Attendance flow

```text
Login → tenant/project authorization → policy → today's assignment/schedule
      → face liveness + 1:1 verification → GPS accuracy → Haversine distance
      → radius → IN/OUT sequence → idempotency → immutable attendance record
```

`OUT` requires a successful `IN` for the same user and day. Duplicate `IN` or `OUT`
must be rejected. The attendance response stores punch coordinates, configured
location coordinates, calculated distance, allowed radius, punch type, timestamp,
face verification ID, and rejection reason when applicable.

## Face recognition

Face recognition is an isolated service. Enrollment requires consent, quality checks,
liveness, encrypted embeddings, model versioning, and retention/deletion support.
Attendance uses short-lived signed verification results. Raw images and embeddings
must never be logged. The POC currently uses a mock adapter so the UI and business
flow can be tested before connecting a real provider.

## Security and non-functional requirements

- OAuth2/OIDC, short-lived JWTs, role and tenant/project authorization.
- Database-per-service in the target microservice deployment.
- For the initial POC, Hibernate `ddl-auto=update` creates missing tables and updates
  the schema at startup without dropping existing data. Production will use controlled
  migrations and must not rely on automatic schema changes.
- Redis for short-lived challenges/idempotency and Kafka/RabbitMQ for events.
- Backend GPS validation, accuracy/freshness checks, rate limiting, replay protection,
  audit logs, encryption at rest, and biometric consent/revocation.
- Reporting, import processing, and notifications should be asynchronous.

## Current POC scope

The current app provides four static users, including a current-location demo user,
four camps, JSP camera capture, browser face descriptors, database-backed face
enrollment/verification, IN/OUT punching, assignment/radius validation, and JPA
entities for automatic POC schema creation. Attendance runtime storage is still
in-memory; the next implementation step is replacing that adapter with the
`attendance_records` repository.

The POC descriptor is generated in the browser and verified on the server. It is
not yet production-grade liveness or anti-spoof protection; production must move
model inference into the isolated Face Service and add consent, liveness, replay
protection, encryption, and retention controls.
