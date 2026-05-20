CREATE TABLE policy_rule_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);

CREATE TABLE policy_rules (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               UUID NOT NULL,
    effect               TEXT NOT NULL CHECK (effect IN ('allow_read', 'deny_write', 'require_approval')),
    resource_pattern     TEXT NOT NULL,
    priority             INT NOT NULL,
    source               TEXT NOT NULL CHECK (source IN ('platform_seed', 'install_pack', 'admin')),
    installation_id      UUID,
    policy_rule_group_id UUID REFERENCES policy_rule_groups (id) ON DELETE CASCADE,
    connector_key        TEXT,
    pack_version         INT,
    enabled              BOOLEAN NOT NULL DEFAULT true,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE policy_rule_bindings (
    rule_id       UUID PRIMARY KEY REFERENCES policy_rules (id) ON DELETE CASCADE,
    workspace_id  UUID NOT NULL
);

CREATE INDEX policy_rules_org_idx ON policy_rules (org_id);
CREATE INDEX policy_rules_installation_idx ON policy_rules (installation_id);

CREATE TABLE pending_approvals (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               UUID NOT NULL,
    workspace_id         UUID NOT NULL,
    requested_by_user_id UUID,
    agent_run_id         UUID,
    tool_name            TEXT NOT NULL,
    tool_arguments       JSONB,
    status               TEXT NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'approved', 'rejected')),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_at           TIMESTAMPTZ,
    decided_by_user_id   UUID
);

CREATE INDEX pending_approvals_org_ws_idx ON pending_approvals (org_id, workspace_id, status);
