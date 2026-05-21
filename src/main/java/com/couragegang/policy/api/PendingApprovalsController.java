package com.couragegang.policy.api;

import com.couragegang.policy.api.dto.PolicyModels.DecideRequest;
import com.couragegang.policy.api.dto.PolicyModels.PendingApprovalView;
import com.couragegang.policy.service.PendingApprovalService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.UUID;

@Controller("/pending-approvals")
public class PendingApprovalsController {

    private final PendingApprovalService pending;

    public PendingApprovalsController(PendingApprovalService pending) {
        this.pending = pending;
    }

    @Get("/{id}")
    public PendingApprovalView get(@PathVariable UUID id) {
        return pending.get(id);
    }

    @Post("/{id}/approve")
    public PendingApprovalView approve(
            @PathVariable UUID id, @Body @Valid @Nullable DecideRequest body) {
        var decidedBy = body != null ? body.decidedByUserId() : null;
        return pending.approve(id, decidedBy);
    }

    @Post("/{id}/reject")
    public PendingApprovalView reject(@PathVariable UUID id, @Body @Valid @Nullable DecideRequest body) {
        var decidedBy = body != null ? body.decidedByUserId() : null;
        return pending.reject(id, decidedBy);
    }
}
