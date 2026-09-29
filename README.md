# Attendance Platform

Scalable, multi-tenant attendance platform planned around daily camp assignments,
backend GPS validation, configurable attendance policies, and isolated face
recognition.

The architecture and security decisions are documented in
[docs/architecture.md](docs/architecture.md).

## Current status

This repository contains the initial Spring Boot bootstrap. The next implementation
slice is to scaffold the service contracts and build the core path:

`Identity -> Tenant/Project -> Policy/Location -> Assignment -> Face Verification -> Attendance`

Face recognition will be a separate service. Attendance will consume a short-lived
signed verification result and will remain the final authority for allow/reject
decisions.
