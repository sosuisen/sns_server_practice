package com.example.security;

import com.example.user.Role;

import io.quarkus.security.PermissionChecker;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * "admin-or-self" パーミッションチェッカー。
 * リクエスト元が admin ロールを持つか、または操作対象ユーザー本人であるかを検証する。
 */
@ApplicationScoped
public class AdminOrSelfPermissionChecker {

    /**
     * リクエスト元が admin または操作対象本人かを検証する。
     *
     * @param identity 現在のセキュリティアイデンティティ
     * @param id       操作対象ユーザーの ID
     * @return admin または本人の場合 {@code true}
     */
    @PermissionChecker("admin-or-self")
    public boolean isAdminOrSelf(SecurityIdentity identity, Long id) {
        if (identity.hasRole(Role.ADMIN)) {
            return true;
        }
        return String.valueOf(id).equals(identity.getPrincipal().getName());
    }
}
