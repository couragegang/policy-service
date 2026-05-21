package com.couragegang.policy.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PolicyModels {

    private PolicyModels() {}

    @Serdeable
    public record ApplyInstallPackRequest(
            @NotNull UUID orgId,
            @NotNull UUID workspaceId,
            @NotNull String connectorKey,
            int policyPackVersion,
            @NotNull Map<String, Object> pack,
            UUID installedByUserId) {}

    @Serdeable
    public record ApplyInstallPackResponse(UUID policyRuleGroupId, int rulesCreated) {}

    @Serdeable
    public record PolicyRuleView(
            UUID id,
            UUID orgId,
            String effect,
            String resourcePattern,
            int priority,
            String source,
            @Nullable UUID installationId,
            @Nullable String connectorKey,
            List<UUID> workspaceIds) {}

    @Serdeable
    public record PolicyRuleListResponse(List<PolicyRuleView> items) {}

    @Serdeable
    public record PendingApprovalView(
            UUID id,
            UUID orgId,
            UUID workspaceId,
            @Nullable UUID requestedByUserId,
            @Nullable UUID agentRunId,
            String toolName,
            @Nullable Map<String, Object> toolArguments,
            String status,
            Instant createdAt,
            @Nullable Instant decidedAt,
            @Nullable UUID decidedByUserId) {}

    @Serdeable
    public record PendingApprovalListResponse(List<PendingApprovalView> items) {}

    @Serdeable
    public record EvaluateRequest(
            @NotNull UUID orgId,
            @NotNull UUID workspaceId,
            @NotBlank String connectorKey,
            @NotBlank String toolName,
            @Nullable Map<String, Object> toolArguments,
            @Nullable UUID userId,
            @Nullable UUID agentRunId) {}

    @Serdeable
    public record EvaluateResponse(
            String decision,
            @Nullable UUID matchedRuleId,
            @Nullable UUID pendingApprovalId) {}

    @Serdeable
    public record CreatePendingRequest(
            @NotNull UUID orgId,
            @NotNull UUID workspaceId,
            @NotBlank String toolName,
            @Nullable Map<String, Object> toolArguments,
            @Nullable UUID requestedByUserId,
            @Nullable UUID agentRunId) {}

    @Serdeable
    public record DecideRequest(@Nullable UUID decidedByUserId) {}

    @Serdeable
    public record ErrorBody(String code, String message) {
        public static ErrorBody of(String code, String message) {
            return new ErrorBody(code, message);
        }
    }
}
