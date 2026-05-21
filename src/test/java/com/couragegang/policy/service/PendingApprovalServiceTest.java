package com.couragegang.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.couragegang.policy.api.dto.PolicyModels.CreatePendingRequest;
import com.couragegang.policy.repo.PendingApprovalRepository;
import com.couragegang.policy.repo.PendingApprovalRow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendingApprovalServiceTest {

    @Mock
    PendingApprovalRepository repo;

    PendingApprovalService svc;

    UUID orgId = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new PendingApprovalService(repo);
    }

    @Test
    void createReturnsId() throws Exception {
        when(repo.insert(eq(orgId), eq(wsId), eq(userId), any(), eq("tool"), any())).thenReturn(id);

        var created = svc.create(new CreatePendingRequest(orgId, wsId, "tool", Map.of(), userId, null));

        assertThat(created).isEqualTo(id);
    }

    @Test
    void listPending() throws Exception {
        when(repo.listPending(orgId, wsId)).thenReturn(List.of(row("pending")));
        when(repo.parseArgs(any())).thenReturn(Map.of("a", 1));

        var list = svc.list(orgId, wsId);

        assertThat(list.items()).hasSize(1);
        assertThat(list.items().getFirst().status()).isEqualTo("pending");
    }

    @Test
    void getNotFound() throws Exception {
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> svc.get(id)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void approveUpdatesStatus() throws Exception {
        when(repo.decide(id, "approved", userId)).thenReturn(true);
        when(repo.findById(id)).thenReturn(Optional.of(row("approved")));
        when(repo.parseArgs(any())).thenReturn(Map.of());

        var view = svc.approve(id, userId);

        assertThat(view.status()).isEqualTo("approved");
    }

    @Test
    void rejectFailsWhenAlreadyDecided() throws Exception {
        when(repo.decide(id, "rejected", userId)).thenReturn(false);

        assertThatThrownBy(() -> svc.reject(id, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    private PendingApprovalRow row(String status) {
        return new PendingApprovalRow(
                id,
                orgId,
                wsId,
                userId,
                null,
                "notion_write_page",
                "{}",
                status,
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                null);
    }
}
