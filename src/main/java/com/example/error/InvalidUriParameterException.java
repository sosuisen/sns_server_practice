package com.example.error;

/**
 * URI由来のパラメーター（クエリ・パス）が不正な場合のドメイン例外。
 */
public class InvalidUriParameterException extends RuntimeException {

    private final String code;

    /**
     * {@code InvalidUriParameterException}を生成する。
     *
     * @param message 例外メッセージ
     * @param code    違反を表す{@link ErrorCode}定数
     */
    public InvalidUriParameterException(String message, String code) {
        super(message);
        this.code = code;
    }

    /**
     * 違反コードを返す。
     *
     * @return {@link ErrorCode}定数
     */
    public String code() {
        return code;
    }
}
