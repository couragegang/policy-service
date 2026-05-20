package com.couragegang.policy.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.util.Map;

@Controller
public final class HealthInfoController {

    @Get("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "policy-service",
                "health", "/v1/policy/health",
                "internal", "/v1/policy/internal/installations/{id}/apply-pack");
    }
}
