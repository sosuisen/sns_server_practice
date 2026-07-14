package com.example.auth;

/**
 * 認証トークンレスポンス。
 *
 * @param jwt JWT
 */
public record TokenResponse(String jwt) {
}
