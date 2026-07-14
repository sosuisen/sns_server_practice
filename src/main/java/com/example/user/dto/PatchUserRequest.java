package com.example.user.dto;

import com.example.error.ErrorCode;
import com.example.validator.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * PATCH /users/{id}のリクエスト本体。
 * nullのフィールドは更新されない。
 */
public record PatchUserRequest(
        @ValidUsername String username,
        @ValidEmail @Schema(format = "email") String email,
        @ValidPassword @Schema(description = "8〜64文字。英字・数字・記号をそれぞれ1文字以上含む") String password) {

    // 派生プロパティへの制約。
    // この制約を再利用する場合は、クラスレベルの制約にすることも検討する。
    // @JsonIgnoreを付けた理由は、getterとみなされOpenAPIスキーマに漏出するのを防ぐため（検証には影響しない）。
    @JsonIgnore
    @AssertTrue(message = ErrorCode.AT_LEAST_ONE_FIELD_REQUIRED)
    public boolean isAtLeastOneFieldProvided() {
        return username != null || email != null || password != null;
    }
}
