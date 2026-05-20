package com.couragegang.policy.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class PolicyModels {

    private PolicyModels() {}

    @Serdeable
    public record ApplyInstallPackRequest(
            @NotNull UUID orgId,
            @NotNull UUID workspaceId,
            @NotNull String connectorKey,
            int policyPackVersion,
            @NotNull JsonNode pack,
            UUID installedByUserId) {}

    @Serdeable
    public record ApplyInstallPackResponse(UUID policyRuleGroupId, int rulesCreated) {}

    @Serdeable
    public record ErrorBody(String code, String message) {
        public static ErrorBody of(String code, String message) {
            return new ErrorBody(code, message);
        }
    }
}
