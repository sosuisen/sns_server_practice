package com.example.user.dto;

import com.example.user.Role;
import com.example.user.User;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;

/**
 * ユーザーレスポンス。
 *
 * <p>
 * Panacheの{@code project()}による射影先としても利用する。各フィールド名は
 * {@link User}のフィールド名と一致するため{@code @ProjectedFieldName}は不要。
 * 射影ではPanacheがコンストラクターをリフレクションで呼び出すため、
 * nativeイメージ向けに{@link RegisterForReflection}を付与する。
 *
 * @param id         ユーザーID
 * @param username   ユーザー名
 * @param email      メールアドレス
 * @param role       ロール（admin / user）
 * @param createdAt  作成日時
 * @param modifiedAt 最終更新日時
 */
@RegisterForReflection
public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt,
        Instant modifiedAt) {
    /**
     * UserエンティティーからUserResponseを生成する。
     *
     * @param user Userエンティティー
     * @return UserResponse
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id, user.username, user.email,
                user.role, user.createdAt, user.modifiedAt);
    }
}
