package com.couragegang.policy.repo;

import java.util.UUID;

public record PolicyRuleRow(
        UUID id,
        UUID orgId,
        String effect,
        String resourcePattern,
        int priority,
        String source,
        UUID installationId,
        String connectorKey) {}
