package com.example.constraint;

/**
 * メッセージの制約値定数。
 *
 * <p>
 * エンティティとバリデーションの双方から参照される。
 * アノテーション属性はコンパイル時定数式しか受け付けないため、
 * static finalフィールドとして定義する。
 */
public final class MessageConstraints {

    private MessageConstraints() {
    }

    /** メッセージ本文の最大文字数。 */
    public static final int BODY_MAX_LENGTH = 140;

    /** メッセージ一覧取得のデフォルト件数。 */
    public static final int DEFAULT_LIST_LIMIT = 50;

    /** メッセージ一覧取得の最小件数。 */
    public static final int MIN_LIST_LIMIT = 1;

    /** メッセージ一覧取得の最大件数。 */
    public static final int MAX_LIST_LIMIT = 100;
}
