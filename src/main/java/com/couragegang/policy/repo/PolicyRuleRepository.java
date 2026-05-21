package com.couragegang.policy.repo;

import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

@Singleton
public final class PolicyRuleRepository {

    private final DataSource dataSource;

    public PolicyRuleRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UUID insertGroup() throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement("INSERT INTO policy_rule_groups DEFAULT VALUES RETURNING id")) {
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getObject(1, UUID.class);
            }
        }
    }

    public int countByInstallation(UUID installationId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM policy_rules WHERE installation_id = ? AND source = 'install_pack'")) {
            ps.setObject(1, installationId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public UUID insertRule(
            UUID orgId,
            String effect,
            String resourcePattern,
            int priority,
            UUID installationId,
            UUID groupId,
            String connectorKey,
            int packVersion)
            throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        INSERT INTO policy_rules
                          (org_id, effect, resource_pattern, priority, source, installation_id,
                           policy_rule_group_id, connector_key, pack_version)
                        VALUES (?, ?, ?, ?, 'install_pack', ?, ?, ?, ?)
                        RETURNING id
                        """)) {
            ps.setObject(1, orgId);
            ps.setString(2, effect);
            ps.setString(3, resourcePattern);
            ps.setInt(4, priority);
            ps.setObject(5, installationId);
            ps.setObject(6, groupId);
            ps.setString(7, connectorKey);
            ps.setInt(8, packVersion);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getObject(1, UUID.class);
            }
        }
    }

    public void insertBinding(UUID ruleId, UUID workspaceId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        "INSERT INTO policy_rule_bindings (rule_id, workspace_id) VALUES (?, ?)")) {
            ps.setObject(1, ruleId);
            ps.setObject(2, workspaceId);
            ps.executeUpdate();
        }
    }

    public int revokeByInstallation(UUID installationId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        "DELETE FROM policy_rules WHERE installation_id = ? AND source = 'install_pack'")) {
            ps.setObject(1, installationId);
            return ps.executeUpdate();
        }
    }

    public List<PolicyRuleRow> listForOrg(UUID orgId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT id, org_id, effect, resource_pattern, priority, source, installation_id, connector_key
                        FROM policy_rules
                        WHERE org_id = ? AND enabled = true
                        ORDER BY priority DESC, created_at ASC
                        """)) {
            ps.setObject(1, orgId);
            var rows = new ArrayList<PolicyRuleRow>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(
                            new PolicyRuleRow(
                                    rs.getObject("id", UUID.class),
                                    rs.getObject("org_id", UUID.class),
                                    rs.getString("effect"),
                                    rs.getString("resource_pattern"),
                                    rs.getInt("priority"),
                                    rs.getString("source"),
                                    rs.getObject("installation_id", UUID.class),
                                    rs.getString("connector_key")));
                }
            }
            return rows;
        }
    }

    public Map<UUID, List<UUID>> bindingsByOrg(UUID orgId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT b.rule_id, b.workspace_id
                        FROM policy_rule_bindings b
                        JOIN policy_rules r ON r.id = b.rule_id
                        WHERE r.org_id = ?
                        """)) {
            ps.setObject(1, orgId);
            var map = new HashMap<UUID, List<UUID>>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var ruleId = rs.getObject("rule_id", UUID.class);
                    var wsId = rs.getObject("workspace_id", UUID.class);
                    map.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(wsId);
                }
            }
            return map;
        }
    }

    public List<PolicyRuleRow> listForWorkspace(UUID orgId, UUID workspaceId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT r.id, r.org_id, r.effect, r.resource_pattern, r.priority, r.source,
                               r.installation_id, r.connector_key
                        FROM policy_rules r
                        JOIN policy_rule_bindings b ON b.rule_id = r.id
                        WHERE r.org_id = ? AND b.workspace_id = ? AND r.enabled = true
                        ORDER BY r.priority DESC, r.created_at ASC
                        """)) {
            ps.setObject(1, orgId);
            ps.setObject(2, workspaceId);
            var rows = new ArrayList<PolicyRuleRow>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(
                            new PolicyRuleRow(
                                    rs.getObject("id", UUID.class),
                                    rs.getObject("org_id", UUID.class),
                                    rs.getString("effect"),
                                    rs.getString("resource_pattern"),
                                    rs.getInt("priority"),
                                    rs.getString("source"),
                                    rs.getObject("installation_id", UUID.class),
                                    rs.getString("connector_key")));
                }
            }
            return rows;
        }
    }
}
