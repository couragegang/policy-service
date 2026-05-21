package com.couragegang.policy.service;

import com.couragegang.policy.api.dto.PolicyModels.CreatePendingRequest;
import com.couragegang.policy.api.dto.PolicyModels.EvaluateRequest;
import com.couragegang.policy.api.dto.PolicyModels.EvaluateResponse;
import com.couragegang.policy.repo.PolicyRuleRepository;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.Map;

@Singleton
public final class PolicyEvaluateService {

    private final PolicyRuleRepository rules;
    private final PendingApprovalService pending;

    public PolicyEvaluateService(PolicyRuleRepository rules, PendingApprovalService pending) {
        this.rules = rules;
        this.pending = pending;
    }

    public EvaluateResponse evaluate(EvaluateRequest req) {
        try {
            var rows = rules.listForWorkspace(req.orgId(), req.workspaceId());
            var writeLike = PolicyMatcher.isWriteLike(req.toolName());
            for (var rule : rows) {
                if (!PolicyMatcher.matches(rule, req.connectorKey(), req.toolName(), writeLike)) {
                    continue;
                }
                return switch (rule.effect()) {
                    case "deny_write" -> new EvaluateResponse("deny", rule.id(), null);
                    case "require_approval" -> {
                        var pendingId =
                                pending.create(
                                        new CreatePendingRequest(
                                                req.orgId(),
                                                req.workspaceId(),
                                                req.toolName(),
                                                req.toolArguments() != null ? req.toolArguments() : Map.of(),
                                                req.userId(),
                                                req.agentRunId()));
                        yield new EvaluateResponse("require_approval", rule.id(), pendingId);
                    }
                    case "allow_read" -> new EvaluateResponse("allow", rule.id(), null);
                    default -> new EvaluateResponse("allow", rule.id(), null);
                };
            }
            return new EvaluateResponse("allow", null, null);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
