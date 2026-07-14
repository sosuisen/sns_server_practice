package com.example.user.dto;

import com.example.error.ErrorCode;
import com.example.user.Role;
import jakarta.validation.constraints.*;

/**
 * PATCH /users/{id}/roleのリクエスト本体。
 *
 * <p>
 * {@code role}が{@code Role}型の許可値以外の場合、デシリアライズ段階で失敗し、
 * {@code MismatchedInputException}が発生する。
 * この例外は{@code ErrorMappers}で400 Bad Requestに変換される。
 *
 * @param role 変更後のロール
 */
public record RoleRequest(
        @NotNull(message = ErrorCode.ROLE_REQUIRED) Role role) {
}
