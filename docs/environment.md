# Environment configuration

Spring Boot does not automatically read an arbitrary `.env` file. For local
development, keep the file at the repository root as `.env`, then export it before
starting the application:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

Use [.env.example](../.env) as the template. The old file under
`untitled/src/.env` belongs to the unused scratch Maven project and should not be
used by the attendance application.

The database credential that was previously present in a comment must be considered
exposed. Rotate that PostgreSQL password before using the database again.
