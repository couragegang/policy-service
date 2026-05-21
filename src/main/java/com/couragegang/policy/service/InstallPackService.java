package com.couragegang.policy.service;

import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackRequest;
import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackResponse;
import com.couragegang.policy.repo.PolicyRuleRepository;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public final class InstallPackService {

    private final PolicyRuleRepository rules;

    public InstallPackService(PolicyRuleRepository rules) {
        this.rules = rules;
    }

    public ApplyInstallPackResponse apply(UUID installationId, ApplyInstallPackRequest req) {
        try {
            if (rules.countByInstallation(installationId) > 0) {
                var groupId = UUID.randomUUID();
                return new ApplyInstallPackResponse(groupId, 0);
            }
            var groupId = rules.insertGroup();
            var created = 0;
            var packRules = req.pack().get("rules");
            if (packRules instanceof List<?> list) {
                for (var item : list) {
                    if (!(item instanceof Map<?, ?> raw)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    var node = (Map<String, Object>) raw;
                    var effect = text(node, "effect");
                    var pattern = text(node, "resource_pattern");
                    var priority = priority(node);
                    var ruleId =
                            rules.insertRule(
                                    req.orgId(),
                                    effect,
                                    pattern,
                                    priority,
                                    installationId,
                                    groupId,
                                    req.connectorKey(),
                                    req.policyPackVersion());
                    rules.insertBinding(ruleId, req.workspaceId());
                    created++;
                }
            }
            return new ApplyInstallPackResponse(groupId, created);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void revoke(UUID installationId) {
        try {
            rules.revokeByInstallation(installationId);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String text(Map<String, Object> node, String field) {
        var v = node.get(field);
        if (v == null) {
            throw new IllegalArgumentException("pack rule missing " + field);
        }
        return v.toString();
    }

    private static int priority(Map<String, Object> node) {
        var v = node.get("priority");
        if (v instanceof Number n) {
            return n.intValue();
        }
        return 100;
    }
}
