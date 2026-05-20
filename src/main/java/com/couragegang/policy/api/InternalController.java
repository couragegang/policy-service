package com.couragegang.policy.api;

import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackRequest;
import com.couragegang.policy.api.dto.PolicyModels.ApplyInstallPackResponse;
import com.couragegang.policy.service.InstallPackService;
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

    public InternalController(InstallPackService installPack) {
        this.installPack = installPack;
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
