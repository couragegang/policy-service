package com.couragegang.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.couragegang.policy.api.dto.PolicyModels.CreatePendingRequest;
import com.couragegang.policy.api.dto.PolicyModels.EvaluateRequest;
import com.couragegang.policy.repo.PolicyRuleRepository;
import com.couragegang.policy.repo.PolicyRuleRow;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyEvaluateServiceTest {

    @Mock
    PolicyRuleRepository rules;

    @Mock
    PendingApprovalService pending;

    PolicyEvaluateService svc;

    UUID orgId = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new PolicyEvaluateService(rules, pending);
    }

    @Test
    void evaluateAllowWhenNoRules() throws Exception {
        when(rules.listForWorkspace(orgId, wsId)).thenReturn(List.of());

        var res = svc.evaluate(new EvaluateRequest(orgId, wsId, "notion", "read_page", Map.of(), userId, null));

        assertThat(res.decision()).isEqualTo("allow");
    }

    @Test
    void evaluateDenyWrite() throws Exception {
        var ruleId = UUID.randomUUID();
        when(rules.listForWorkspace(orgId, wsId))
                .thenReturn(List.of(new PolicyRuleRow(ruleId, orgId, "deny_write", "mcp:notion:*:write", 1, "p", null, "notion")));

        var res = svc.evaluate(new EvaluateRequest(orgId, wsId, "notion", "notion_write_page", Map.of(), userId, null));

        assertThat(res.decision()).isEqualTo("deny");
        assertThat(res.matchedRuleId()).isEqualTo(ruleId);
    }

    @Test
    void evaluateAllowReadEffect() throws Exception {
        var ruleId = UUID.randomUUID();
        when(rules.listForWorkspace(orgId, wsId))
                .thenReturn(List.of(new PolicyRuleRow(ruleId, orgId, "allow_read", "mcp:notion:*:read", 1, "p", null, "notion")));

        var res = svc.evaluate(new EvaluateRequest(orgId, wsId, "notion", "read_tool", Map.of(), userId, null));

        assertThat(res.decision()).isEqualTo("allow");
        assertThat(res.matchedRuleId()).isEqualTo(ruleId);
    }

    @Test
    void evaluateUnknownEffectDefaultsToAllow() throws Exception {
        var ruleId = UUID.randomUUID();
        when(rules.listForWorkspace(orgId, wsId))
                .thenReturn(List.of(new PolicyRuleRow(ruleId, orgId, "custom_effect", "tool:foo", 1, "p", null, "notion")));

        var res = svc.evaluate(new EvaluateRequest(orgId, wsId, "notion", "foo", Map.of(), userId, null));

        assertThat(res.decision()).isEqualTo("allow");
    }

    @Test
    void evaluateRequireApprovalCreatesPending() throws Exception {
        var ruleId = UUID.randomUUID();
        var pendingId = UUID.randomUUID();
        when(rules.listForWorkspace(orgId, wsId))
                .thenReturn(
                        List.of(
                                new PolicyRuleRow(
                                        ruleId, orgId, "require_approval", "mcp:notion:*:write", 1, "p", null, "notion")));
        when(pending.create(any(CreatePendingRequest.class))).thenReturn(pendingId);

        var res =
                svc.evaluate(
                        new EvaluateRequest(orgId, wsId, "notion", "notion_write_page", Map.of("k", "v"), userId, null));

        assertThat(res.decision()).isEqualTo("require_approval");
        assertThat(res.pendingApprovalId()).isEqualTo(pendingId);
        verify(pending).create(any(CreatePendingRequest.class));
    }

    @Test
    void evaluateSkipsNonMatchingRule() throws Exception {
        when(rules.listForWorkspace(orgId, wsId))
                .thenReturn(List.of(new PolicyRuleRow(UUID.randomUUID(), orgId, "deny_write", "mcp:slack:*:write", 1, "p", null, "slack")));

        var res = svc.evaluate(new EvaluateRequest(orgId, wsId, "notion", "notion_write_page", Map.of(), userId, null));

        assertThat(res.decision()).isEqualTo("allow");
    }

    @Test
    void evaluateWrapsSqlException() throws Exception {
        when(rules.listForWorkspace(orgId, wsId)).thenThrow(new SQLException("db"));

        assertThatThrownBy(() -> svc.evaluate(new EvaluateRequest(orgId, wsId, "notion", "t", null, null, null)))
                .isInstanceOf(IllegalStateException.class);
    }
}
