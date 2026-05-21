package com.couragegang.policy.api;

import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackRequest;
import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackResponse;
import com.couragegang.policy.api.dto.PolicyModels.CreatePendingRequest;
import com.couragegang.policy.api.dto.PolicyModels.EvaluateRequest;
import com.couragegang.policy.api.dto.PolicyModels.EvaluateResponse;
import com.couragegang.policy.service.InstallPackService;
import com.couragegang.policy.service.PendingApprovalService;
import com.couragegang.policy.service.PolicyEvaluateService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import java.util.UUID;

@Controller("/internal")
public class InternalController {

    private final InstallPackService installPack;
    private final PolicyEvaluateService evaluate;
    private final PendingApprovalService pending;

    public InternalController(
            InstallPackService installPack,
            PolicyEvaluateService evaluate,
            PendingApprovalService pending) {
        this.installPack = installPack;
        this.evaluate = evaluate;
        this.pending = pending;
    }

    @Post("/evaluate")
    public EvaluateResponse evaluateTool(@Body @Valid EvaluateRequest body) {
        return evaluate.evaluate(body);
    }

    @Post("/pending-approvals")
    public HttpResponse<UUID> createPending(@Body @Valid CreatePendingRequest body) {
        return HttpResponse.created(pending.create(body));
    }

    @Post("/installations/{installationId}/apply-pack")
    public HttpResponse<ApplyInstallPackResponse> applyPack(
            @PathVariable UUID installationId, @Body @Valid ApplyInstallPackRequest body) {
        return HttpResponse.ok(installPack.apply(installationId, body));
    }

    @Delete("/installations/{installationId}/revoke-pack")
    public HttpResponse<Void> revokePack(@PathVariable UUID installationId) {
        installPack.revoke(installationId);
        return HttpResponse.noContent();
    }
}
