package com.pharma.infrastructure.security;

public final class RoleAuthorityUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    private RoleAuthorityUtils() {
    }

    public static String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "";
        }
        return roleName.startsWith(ROLE_PREFIX)
                ? roleName.substring(ROLE_PREFIX.length())
                : roleName;
    }

    public static String toAuthority(String roleName) {
        String normalized = normalizeRoleName(roleName);
        return normalized.isEmpty() ? "" : ROLE_PREFIX + normalized;
    }
}
