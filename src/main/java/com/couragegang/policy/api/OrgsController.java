package com.couragegang.policy.api;

import com.couragegang.policy.api.dto.PolicyModels.PolicyRuleListResponse;
import com.couragegang.policy.api.dto.PolicyModels.PendingApprovalListResponse;
import com.couragegang.policy.service.PendingApprovalService;
import com.couragegang.policy.service.PolicyQueryService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.annotation.Nullable;
import java.util.UUID;

@Controller("/orgs")
public class OrgsController {

    private final PolicyQueryService rules;
    private final PendingApprovalService pending;

    public OrgsController(PolicyQueryService rules, PendingApprovalService pending) {
        this.rules = rules;
        this.pending = pending;
    }

    @Get("/{orgId}/rules")
    public PolicyRuleListResponse listRules(@PathVariable UUID orgId) {
        return rules.listRules(orgId);
    }

    @Get("/{orgId}/pending-approvals")
    public PendingApprovalListResponse listPending(
            @PathVariable UUID orgId, @QueryValue("workspace_id") @Nullable UUID workspaceId) {
        return pending.list(orgId, workspaceId);
    }
}
