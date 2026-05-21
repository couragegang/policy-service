package com.couragegang.policy.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

@Singleton
public final class PendingApprovalRepository {

    private final DataSource dataSource;
    private final ObjectMapper json;

    public PendingApprovalRepository(DataSource dataSource, ObjectMapper json) {
        this.dataSource = dataSource;
        this.json = json;
    }

    public UUID insert(
            UUID orgId,
            UUID workspaceId,
            UUID requestedByUserId,
            UUID agentRunId,
            String toolName,
            Map<String, Object> toolArguments)
            throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        INSERT INTO pending_approvals
                          (org_id, workspace_id, requested_by_user_id, agent_run_id, tool_name, tool_arguments)
                        VALUES (?, ?, ?, ?, ?, ?::jsonb)
                        RETURNING id
                        """)) {
            ps.setObject(1, orgId);
            ps.setObject(2, workspaceId);
            setUuid(ps, 3, requestedByUserId);
            setUuid(ps, 4, agentRunId);
            ps.setString(5, toolName);
            ps.setString(6, toJson(toolArguments));
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getObject(1, UUID.class);
            }
        }
    }

    public List<PendingApprovalRow> listPending(UUID orgId, UUID workspaceId) throws SQLException {
        var sql =
                """
                SELECT id, org_id, workspace_id, requested_by_user_id, agent_run_id, tool_name,
                       tool_arguments::text, status, created_at, decided_at, decided_by_user_id
                FROM pending_approvals
                WHERE org_id = ? AND status = 'pending'
                """;
        if (workspaceId != null) {
            sql += " AND workspace_id = ?";
        }
        sql += " ORDER BY created_at DESC";
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setObject(1, orgId);
            if (workspaceId != null) {
                ps.setObject(2, workspaceId);
            }
            var rows = new ArrayList<PendingApprovalRow>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
            return rows;
        }
    }

    public Optional<PendingApprovalRow> findById(UUID id) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT id, org_id, workspace_id, requested_by_user_id, agent_run_id, tool_name,
                               tool_arguments::text, status, created_at, decided_at, decided_by_user_id
                        FROM pending_approvals WHERE id = ?
                        """)) {
            ps.setObject(1, id);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public boolean decide(UUID id, String status, UUID decidedByUserId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        UPDATE pending_approvals
                        SET status = ?, decided_at = now(), decided_by_user_id = ?
                        WHERE id = ? AND status = 'pending'
                        """)) {
            ps.setString(1, status);
            setUuid(ps, 2, decidedByUserId);
            ps.setObject(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    private PendingApprovalRow mapRow(java.sql.ResultSet rs) throws SQLException {
        return new PendingApprovalRow(
                rs.getObject("id", UUID.class),
                rs.getObject("org_id", UUID.class),
                rs.getObject("workspace_id", UUID.class),
                rs.getObject("requested_by_user_id", UUID.class),
                rs.getObject("agent_run_id", UUID.class),
                rs.getString("tool_name"),
                rs.getString("tool_arguments"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("decided_at") != null ? rs.getTimestamp("decided_at").toInstant() : null,
                rs.getObject("decided_by_user_id", UUID.class));
    }

    private void setUuid(java.sql.PreparedStatement ps, int idx, UUID value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.OTHER);
        } else {
            ps.setObject(idx, value);
        }
    }

    private String toJson(Map<String, Object> value) throws SQLException {
        try {
            return json.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public Map<String, Object> parseArgs(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            return Map.of();
        }
        try {
            return json.readValue(jsonText, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
