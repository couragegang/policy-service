package com.couragegang.policy.repo;

import jakarta.inject.Singleton;
import java.sql.SQLException;
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
}
