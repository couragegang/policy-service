package com.couragegang.policy.service;

import com.couragegang.policy.repo.PolicyRuleRow;

final class PolicyMatcher {

    private PolicyMatcher() {}

    static boolean matches(PolicyRuleRow rule, String connectorKey, String toolName, boolean writeLike) {
        var pattern = rule.resourcePattern();
        if (pattern == null || pattern.isBlank()) {
            return false;
        }
        var parts = pattern.split(":");
        if (parts.length < 2) {
            return false;
        }
        if ("mcp".equals(parts[0])) {
            if (parts.length >= 2 && !"*".equals(parts[1]) && !parts[1].equals(connectorKey)) {
                return false;
            }
            if (parts.length >= 4 && "write".equals(parts[3])) {
                return writeLike;
            }
            if (parts.length >= 4 && "read".equals(parts[3])) {
                return !writeLike;
            }
            if (parts.length == 3 && !"*".equals(parts[2]) && parts[2].equals(toolName)) {
                return true;
            }
            return parts.length == 2 || (parts.length == 3 && "*".equals(parts[2]));
        }
        if ("tool".equals(parts[0]) && parts.length >= 2) {
            var spec = parts[1];
            if (spec.endsWith("*")) {
                return toolName.startsWith(spec.substring(0, spec.length() - 1));
            }
            return spec.equals(toolName);
        }
        return false;
    }

    static boolean isWriteLike(String toolName) {
        if (toolName == null) {
            return false;
        }
        var lower = toolName.toLowerCase();
        return lower.contains("write")
                || lower.contains("create")
                || lower.contains("update")
                || lower.contains("delete")
                || lower.contains("post")
                || lower.contains("patch");
    }
}
