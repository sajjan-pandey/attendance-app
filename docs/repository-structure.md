# Enterprise repository structure

The current runnable POC is one bounded context, but its package layout follows the
same boundaries that will become independent microservices.

```text
attendance/
├── docs/
├── contracts/                         # versioned OpenAPI/events (next slice)
├── infra/                             # Docker, DB, broker, observability (next slice)
├── src/main/java/com/attendance/
│   ├── AttendanceApplication.java
│   ├── controller/                    # REST and HTTP/JSP controllers
│   ├── service/                       # use cases and face service boundary
│   ├── dto/
│   │   ├── request/                   # inbound API/request models
│   │   └── response/                  # outbound API/view models
│   ├── mapper/                        # entity <-> DTO conversion
│   ├── entity/                        # persistence/domain models
│   ├── repository/                    # persistence interfaces/adapters
│   └── util/                          # small stateless technical utilities
│
└── src/main/webapp/WEB-INF/jsp/       # server-rendered POC views
```

Naming rules:

- `*Controller` only handles HTTP input/output.
- `*Service` orchestrates a use case; external integrations get a dedicated service
  interface and adapter later.
- `*Repository` owns persistence access.
- `*Request` and `*Response` are DTOs; controllers do not expose entities directly.
- Domain classes use business names (`AttendanceRecord`, `DemoLocation`), not generic
  catch-all model classes.
- The JSP is named after the feature: `attendance-demo.jsp`.

When the POC is split into deployable services, use this monorepo layout:

```text
services/
├── api-gateway/
├── identity-service/
├── tenant-service/
├── workforce-service/
├── policy-location-service/
├── assignment-service/
├── face-service/
└── attendance-service/
```
