package com.couragegang.policy.service;

import com.couragegang.policy.api.dto.PolicyModels.CreatePendingRequest;
import com.couragegang.policy.api.dto.PolicyModels.PendingApprovalListResponse;
import com.couragegang.policy.api.dto.PolicyModels.PendingApprovalView;
import com.couragegang.policy.repo.PendingApprovalRepository;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

@Singleton
public final class PendingApprovalService {

    private final PendingApprovalRepository repo;

    public PendingApprovalService(PendingApprovalRepository repo) {
        this.repo = repo;
    }

    public UUID create(CreatePendingRequest req) {
        try {
            return repo.insert(
                    req.orgId(),
                    req.workspaceId(),
                    req.requestedByUserId(),
                    req.agentRunId(),
                    req.toolName(),
                    req.toolArguments());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public PendingApprovalListResponse list(UUID orgId, UUID workspaceId) {
        try {
            var rows = repo.listPending(orgId, workspaceId);
            var items = new ArrayList<PendingApprovalView>();
            for (var row : rows) {
                items.add(toView(row));
            }
            return new PendingApprovalListResponse(items);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public PendingApprovalView get(UUID id) {
        try {
            return repo.findById(id)
                    .map(this::toView)
                    .orElseThrow(() -> new IllegalArgumentException("not found"));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public PendingApprovalView approve(UUID id, UUID decidedByUserId) {
        return decide(id, "approved", decidedByUserId);
    }

    public PendingApprovalView reject(UUID id, UUID decidedByUserId) {
        return decide(id, "rejected", decidedByUserId);
    }

    private PendingApprovalView decide(UUID id, String status, UUID decidedByUserId) {
        try {
            if (!repo.decide(id, status, decidedByUserId)) {
                throw new IllegalArgumentException("not found or already decided");
            }
            return get(id);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private PendingApprovalView toView(com.couragegang.policy.repo.PendingApprovalRow row) {
        return new PendingApprovalView(
                row.id(),
                row.orgId(),
                row.workspaceId(),
                row.requestedByUserId(),
                row.agentRunId(),
                row.toolName(),
                repo.parseArgs(row.toolArgumentsJson()),
                row.status(),
                row.createdAt(),
                row.decidedAt(),
                row.decidedByUserId());
    }
}
