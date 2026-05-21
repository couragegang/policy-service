package com.couragegang.policy.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.ServerFilterChain;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class InternalApiKeyFilterTest {

    @Test
    void rejectsInvalidKey() {
        var filter = new InternalApiKeyFilter("key");
        var chain = mock(ServerFilterChain.class);

        var response =
                Mono.from(filter.doFilter(HttpRequest.GET("/internal/x").header("X-Policy-Internal-Key", "bad"), chain))
                        .block();

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void proceedsWhenKeyMatches() {
        var filter = new InternalApiKeyFilter("key");
        var chain = mock(ServerFilterChain.class);
        when(chain.proceed(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        var request = HttpRequest.GET("/internal/x").header("X-Policy-Internal-Key", "key");

        Mono.from(filter.doFilter(request, chain)).block();

        verify(chain).proceed(request);
    }
}
