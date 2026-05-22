package com.couragegang.policy.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.policy.repo.PolicyRuleRow;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PolicyMatcherTest {

    private static PolicyRuleRow rule(String effect, String pattern, String connectorKey) {
        return new PolicyRuleRow(UUID.randomUUID(), UUID.randomUUID(), effect, pattern, 1, "pack", null, connectorKey);
    }

    @Test
    void isWriteLike_detectsWriteVerbs() {
        assertThat(PolicyMatcher.isWriteLike(null)).isFalse();
        assertThat(PolicyMatcher.isWriteLike("notion_read")).isFalse();
        assertThat(PolicyMatcher.isWriteLike("notion_write_page")).isTrue();
        assertThat(PolicyMatcher.isWriteLike("create_item")).isTrue();
        assertThat(PolicyMatcher.isWriteLike("UPDATE_x")).isTrue();
        assertThat(PolicyMatcher.isWriteLike("delete_row")).isTrue();
        assertThat(PolicyMatcher.isWriteLike("http_post")).isTrue();
        assertThat(PolicyMatcher.isWriteLike("patch_doc")).isTrue();
    }

    @Test
    void matchesBlankPattern() {
        var r = rule("allow_read", "", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "t", false)).isFalse();
    }

    @Test
    void matchesShortPattern() {
        var r = rule("allow", "mcp", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "t", false)).isFalse();
    }

    @Test
    void matchesMcpWritePattern() {
        var r = rule("require_approval", "mcp:notion:*:write", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "notion_write_page", true)).isTrue();
        assertThat(PolicyMatcher.matches(r, "notion", "notion_read_page", false)).isFalse();
        assertThat(PolicyMatcher.matches(r, "slack", "notion_write_page", true)).isFalse();
    }

    @Test
    void matchesMcpReadPattern() {
        var r = rule("allow_read", "mcp:notion:*:read", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "read_tool", false)).isTrue();
        assertThat(PolicyMatcher.matches(r, "notion", "write_tool", true)).isFalse();
    }

    @Test
    void matchesMcpSpecificToolSegment() {
        var r = rule("allow", "mcp:notion:exact_tool", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "exact_tool", false)).isTrue();
        assertThat(PolicyMatcher.matches(r, "notion", "other", false)).isFalse();
    }

    @Test
    void matchesMcpTwoSegmentPattern() {
        var r = rule("allow", "mcp:notion", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "any", false)).isTrue();
    }

    @Test
    void matchesToolPrefixPattern() {
        var r = rule("deny_write", "tool:notion_*", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "notion_write_page", true)).isTrue();
        assertThat(PolicyMatcher.matches(r, "notion", "slack_write", true)).isFalse();
    }

    @Test
    void matchesExactToolPattern() {
        var r = rule("deny_write", "tool:exact_tool", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "exact_tool", true)).isTrue();
        assertThat(PolicyMatcher.matches(r, "notion", "other", true)).isFalse();
    }

    @Test
    void unknownPatternPrefix() {
        var r = rule("allow", "unknown:foo", "notion");
        assertThat(PolicyMatcher.matches(r, "notion", "t", false)).isFalse();
    }
}
