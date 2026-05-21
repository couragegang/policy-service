package com.couragegang.policy.service;

import com.couragegang.policy.api.dto.PolicyModels.PolicyRuleListResponse;
import com.couragegang.policy.api.dto.PolicyModels.PolicyRuleView;
import com.couragegang.policy.repo.PolicyRuleRepository;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public final class PolicyQueryService {

    private final PolicyRuleRepository rules;

    public PolicyQueryService(PolicyRuleRepository rules) {
        this.rules = rules;
    }

    public PolicyRuleListResponse listRules(UUID orgId) {
        try {
            var rows = rules.listForOrg(orgId);
            var bindings = rules.bindingsByOrg(orgId);
            var items = new ArrayList<PolicyRuleView>();
            for (var row : rows) {
                items.add(
                        new PolicyRuleView(
                                row.id(),
                                row.orgId(),
                                row.effect(),
                                row.resourcePattern(),
                                row.priority(),
                                row.source(),
                                row.installationId(),
                                row.connectorKey(),
                                bindings.getOrDefault(row.id(), List.of())));
            }
            return new PolicyRuleListResponse(items);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
