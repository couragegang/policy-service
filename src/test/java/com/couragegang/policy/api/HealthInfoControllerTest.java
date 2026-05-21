package com.couragegang.policy.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthInfoControllerTest {

    @Test
    void rootContainsServiceName() {
        var map = new HealthInfoController().root();
        assertThat(map.get("service")).isEqualTo("policy-service");
        assertThat(map).containsKey("rules");
    }
}
