package com.example.auth;

import com.example.error.ErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエスト。
 *
 * @param email    メールアドレス
 * @param password パスワード
 */
public record LoginRequest(
        @NotBlank @Email(message = ErrorCode.EMAIL_INVALID) String email,
        @NotBlank String password) {
}
