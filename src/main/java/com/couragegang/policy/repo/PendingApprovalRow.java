package com.couragegang.policy.repo;

import java.time.Instant;
import java.util.UUID;

public record PendingApprovalRow(
        UUID id,
        UUID orgId,
        UUID workspaceId,
        UUID requestedByUserId,
        UUID agentRunId,
        String toolName,
        String toolArgumentsJson,
        String status,
        Instant createdAt,
        Instant decidedAt,
        UUID decidedByUserId) {}
