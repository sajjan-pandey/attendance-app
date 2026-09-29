CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE attendance_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(150) NOT NULL,
    policy_type VARCHAR(30) NOT NULL CHECK (policy_type IN ('FIXED_LOCATION','ASSIGNED_LOCATION','FLEXIBLE_LOCATION')),
    default_radius_meters NUMERIC(8,2) NOT NULL DEFAULT 50 CHECK (default_radius_meters > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, name)
);

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    attendance_policy_id UUID REFERENCES attendance_policies(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, code)
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    user_code VARCHAR(80) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, project_id, user_code)
);

CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(80) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('OFFICE','CAMP','CLIENT_SITE','WAREHOUSE','OTHER')),
    latitude NUMERIC(10,7) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude NUMERIC(10,7) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    radius_meters NUMERIC(8,2) NOT NULL DEFAULT 50 CHECK (radius_meters > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, project_id, code)
);

CREATE TABLE camp_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    location_id UUID NOT NULL REFERENCES locations(id),
    schedule_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED')),
    CHECK (end_time > start_time),
    UNIQUE (project_id, location_id, schedule_date)
);

CREATE TABLE user_location_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    user_id UUID NOT NULL REFERENCES app_users(id),
    location_id UUID NOT NULL REFERENCES locations(id),
    assignment_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED')),
    CHECK (end_time IS NULL OR start_time IS NULL OR end_time > start_time),
    UNIQUE (tenant_id, project_id, user_id, assignment_date)
);

CREATE TABLE face_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID NOT NULL REFERENCES app_users(id),
    encrypted_embedding BYTEA NOT NULL,
    model_version VARCHAR(80) NOT NULL,
    consented_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','REVOKED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, user_id)
);

CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    user_id UUID NOT NULL REFERENCES app_users(id),
    location_id UUID REFERENCES locations(id),
    attendance_type VARCHAR(10) NOT NULL CHECK (attendance_type IN ('IN','OUT')),
    punched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    punch_latitude NUMERIC(10,7),
    punch_longitude NUMERIC(10,7),
    location_latitude NUMERIC(10,7),
    location_longitude NUMERIC(10,7),
    calculated_distance_meters NUMERIC(10,2),
    allowed_radius_meters NUMERIC(10,2),
    face_verification_id UUID,
    decision VARCHAR(20) NOT NULL CHECK (decision IN ('ALLOWED','REJECTED')),
    rejection_reason VARCHAR(255),
    idempotency_key VARCHAR(120) NOT NULL,
    UNIQUE (tenant_id, idempotency_key)
);

CREATE INDEX idx_assignments_active_day ON user_location_assignments (tenant_id, project_id, assignment_date, user_id);
CREATE INDEX idx_attendance_user_day ON attendance_records (tenant_id, project_id, user_id, punched_at);
CREATE UNIQUE INDEX uq_attendance_user_type_day
    ON attendance_records (tenant_id, project_id, user_id, attendance_type, (punched_at::date));
