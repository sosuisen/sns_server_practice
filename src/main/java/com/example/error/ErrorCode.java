package com.example.error;

/**
 * APIエラーコード定数。
 *
 * <p>
 * アノテーション属性はコンパイル時定数式しか受け付けないため、
 * static finalフィールドとして定義する。
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    // ユーザー
    public static final String USERNAME_REQUIRED = "USERNAME_REQUIRED";
    public static final String USERNAME_INVALID_SIZE = "USERNAME_INVALID_SIZE";
    public static final String USERNAME_INVALID_CHARS = "USERNAME_INVALID_CHARS";
    public static final String EMAIL_REQUIRED = "EMAIL_REQUIRED";
    public static final String EMAIL_INVALID = "EMAIL_INVALID";
    public static final String EMAIL_TOO_LONG = "EMAIL_TOO_LONG";
    public static final String EMAIL_IN_USE = "EMAIL_IN_USE";
    public static final String PASSWORD_REQUIRED = "PASSWORD_REQUIRED";
    public static final String PASSWORD_INVALID_SIZE = "PASSWORD_INVALID_SIZE";
    public static final String PASSWORD_INVALID_CHARS = "PASSWORD_INVALID_CHARS";
    public static final String PASSWORD_WEAK = "PASSWORD_WEAK";
    public static final String ROLE_REQUIRED = "ROLE_REQUIRED";
    public static final String AT_LEAST_ONE_FIELD_REQUIRED = "AT_LEAST_ONE_FIELD_REQUIRED";

    // メッセージ
    public static final String BODY_REQUIRED = "BODY_REQUIRED";
    public static final String BODY_TOO_LONG = "BODY_TOO_LONG";

    // リクエスト（共通）
    public static final String REQUEST_BODY_REQUIRED = "REQUEST_BODY_REQUIRED";
    public static final String REQUEST_BODY_INVALID = "REQUEST_BODY_INVALID";

    // 一覧（共通）
    public static final String LIMIT_INVALID = "LIMIT_INVALID";
    public static final String SINCE_INVALID = "SINCE_INVALID";

    // DB
    public static final String DATABASE_UNAVAILABLE = "DATABASE_UNAVAILABLE";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
}
