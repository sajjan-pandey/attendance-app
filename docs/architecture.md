# Face Recognition + Microservices Attendance Architecture

This repository is currently a Spring Boot bootstrap project. The target system is a
multi-tenant attendance platform in which location policy and face verification are
independent, deployable capabilities.

## Service boundaries

```text
Mobile/Web Client
        |
   API Gateway  ---- Identity Service (JWT/OIDC, roles, tenant context)
        |
        +---- Tenant/Project Service
        +---- Workforce Service (users, employee profiles)
        +---- Policy & Location Service (policies, camps, schedules)
        +---- Assignment Service (daily user-camp assignments, bulk import)
        +---- Face Service (enrollment, liveness, 1:1 verification)
        +---- Attendance Service (authoritative check-in/check-out decision)
        +---- Reporting/Notification Service

Kafka/RabbitMQ: attendance events, imports, audit events
PostgreSQL per service | Redis: short-lived cache/idempotency | Object storage: imports
```

Each service owns its database and migrations. Services must not read another
service's tables directly. Cross-service data is obtained through APIs or events.

## Request flow

1. The client authenticates through Identity Service and receives a JWT containing
   `sub`, `tenant_id`, roles, and permitted project IDs.
2. The gateway validates the token and forwards the immutable tenant context. A
   caller-supplied tenant ID is never trusted.
3. Attendance Service asks Policy/Location and Assignment services for the current
   policy, active schedule, and user's assignment.
4. Face Service verifies a short-lived challenge against the claimed user. It must
   perform liveness detection and return only a signed verification result, not a
   reusable face image.
5. Attendance Service validates the server-side decision in this order:
   tenant/project authorization -> policy -> schedule/assignment -> GPS accuracy and
   Haversine distance -> face verification -> idempotency -> attendance record.
6. It writes an immutable attendance decision and publishes `AttendanceRecorded`.

For `ASSIGNED_LOCATION`, a user at another valid camp is still rejected because the
assignment check happens before the radius check. For `FLEXIBLE_LOCATION`, the GPS
point may be stored according to the tenant's retention policy and an explicit type
such as `OFFICE`, `WFH`, `CLIENT_SITE`, or `FIELD` is required.

## Face service contract

### Enrollment

`POST /v1/face/enrollments`

```json
{
  "userId": "user-123",
  "challengeId": "challenge-456",
  "images": ["multipart image 1", "multipart image 2"]
}
```

The service validates consent, image quality, liveness, and duplicate enrollment.
It stores an encrypted embedding and metadata (`model_version`, consent timestamp,
tenant ID); raw images are not retained by default.

### Verification

`POST /v1/face/verifications`

```json
{
  "userId": "user-123",
  "challengeId": "challenge-789",
  "image": "multipart image"
}
```

Response:

```json
{
  "verificationId": "ver-001",
  "userId": "user-123",
  "tenantId": "tenant-001",
  "verified": true,
  "livenessPassed": true,
  "expiresAt": "2026-09-16T10:00:30Z",
  "modelVersion": "face-model-v1"
}
```

This is a 1:1 verification against the authenticated user's enrollment. 1:N face
search should not be used for attendance unless a separate, documented use case and
consent policy requires it.

## Core data ownership

- Tenant/Project: tenant, project, status, policy reference.
- Workforce: user, employee code, project membership.
- Policy & Location: policy type, location/camp, latitude, longitude, radius,
  schedules.
- Assignment: daily assignment with a uniqueness key on
  `(tenant_id, project_id, user_id, assignment_date)`.
- Face: encrypted embedding, consent, model version, enrollment status, audit trail.
- Attendance: attendance decision, timestamps, GPS accuracy, computed distance,
  policy result, face verification ID, rejection reason, and idempotency key.

All tenant-owned tables include `tenant_id`. Repository queries must include tenant
scope, and service-to-service calls must carry a signed service identity plus tenant
context. PostgreSQL row-level security is recommended as a second isolation layer.

## Security and privacy requirements

- Use OIDC/OAuth2 and short-lived access tokens; do not implement password handling in
  every service.
- Require explicit biometric consent, tenant-configurable retention, deletion/export,
  and an alternate non-biometric attendance path for approved exceptions.
- Encrypt embeddings at rest with a KMS-managed key; never log images, embeddings,
  tokens, or precise GPS unnecessarily.
- Require liveness, camera quality checks, rate limiting, replay protection, and a
  verification challenge nonce. A face match alone never authorizes attendance.
- Record audit events for enrollment, verification, policy changes, assignment edits,
  and rejected attendance.
- Validate GPS accuracy, timestamp freshness, device attestation where available, and
  calculate distance on the backend.

## Suggested repository layout

```text
services/
  api-gateway/
  identity-service/
  tenant-service/
  workforce-service/
  policy-location-service/
  assignment-service/
  face-service/
  attendance-service/
  reporting-service/
contracts/                  # versioned OpenAPI and event schemas
infra/                      # docker-compose, Kafka, Postgres, Redis, object storage
```

The first implementation slice should be `identity -> tenant/project -> policy/
location -> assignment -> attendance`, with Face Service initially behind a clean
interface and a local development adapter. The production adapter can then use a
managed provider or an isolated GPU service without changing attendance rules.
