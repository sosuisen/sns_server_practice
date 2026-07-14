package com.example.user.dto;

import com.example.error.ErrorCode;
import com.example.validator.*;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * POST /usersのリクエスト本体。
 *
 * @param username ユーザー名
 * @param email    メールアドレス
 * @param password パスワード
 */
public record UserRequest(
        @NotBlank(message = ErrorCode.USERNAME_REQUIRED) @ValidUsername String username,
        @NotBlank(message = ErrorCode.EMAIL_REQUIRED) @ValidEmail @Schema(format = "email") String email,
        @NotBlank(message = ErrorCode.PASSWORD_REQUIRED) @ValidPassword @Schema(description = "8〜64文字。英字・数字・記号をそれぞれ1文字以上含む") String password) {
}
