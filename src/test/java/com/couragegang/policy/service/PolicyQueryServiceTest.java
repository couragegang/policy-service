package com.couragegang.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.couragegang.policy.repo.PolicyRuleRepository;
import com.couragegang.policy.repo.PolicyRuleRow;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyQueryServiceTest {

    @Mock
    PolicyRuleRepository rules;

    PolicyQueryService svc;
    UUID orgId = UUID.randomUUID();
    UUID ruleId = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new PolicyQueryService(rules);
    }

    @Test
    void listRulesWithBindings() throws Exception {
        when(rules.listForOrg(orgId))
                .thenReturn(
                        List.of(
                                new PolicyRuleRow(
                                        ruleId, orgId, "allow_read", "mcp:notion:*:read", 10, "pack", null, "notion")));
        when(rules.bindingsByOrg(orgId)).thenReturn(Map.of(ruleId, List.of(wsId)));

        var page = svc.listRules(orgId);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().getFirst().workspaceIds()).containsExactly(wsId);
    }
}
