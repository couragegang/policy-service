package com.couragegang.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackRequest;
import com.couragegang.policy.repo.PolicyRuleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstallPackServiceTest {

    @Mock
    PolicyRuleRepository rules;

    InstallPackService svc;
    ObjectMapper json = new ObjectMapper();

    UUID orgId = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();
    UUID installationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new InstallPackService(rules);
    }

    @Test
    void applyCreatesRulesFromPack() throws Exception {
        var groupId = UUID.randomUUID();
        var ruleId = UUID.randomUUID();
        when(rules.countByInstallation(installationId)).thenReturn(0);
        when(rules.insertGroup()).thenReturn(groupId);
        when(rules.insertRule(any(), any(), any(), anyInt(), eq(installationId), eq(groupId), eq("notion"), eq(1)))
                .thenReturn(ruleId);

        Map<String, Object> pack =
                json.readValue(
                        """
                        {"rules":[
                          {"effect":"allow_read","resource_pattern":"mcp:notion:*:read","priority":100},
                          {"effect":"require_approval","resource_pattern":"mcp:notion:*:write","priority":200}
                        ]}
                        """,
                        new TypeReference<>() {});
        var req = new ApplyInstallPackRequest(orgId, wsId, "notion", 1, pack, null);

        var result = svc.apply(installationId, req);

        assertThat(result.rulesCreated()).isEqualTo(2);
        assertThat(result.policyRuleGroupId()).isEqualTo(groupId);
        verify(rules, times(2)).insertBinding(eq(ruleId), eq(wsId));
    }

    @Test
    void applyIsIdempotentWhenRulesExist() throws Exception {
        when(rules.countByInstallation(installationId)).thenReturn(2);
        Map<String, Object> pack = json.readValue("{\"rules\":[]}", new TypeReference<>() {});
        var req = new ApplyInstallPackRequest(orgId, wsId, "notion", 1, pack, null);

        var result = svc.apply(installationId, req);

        assertThat(result.rulesCreated()).isZero();
        verify(rules, never()).insertGroup();
    }
}
