package com.example.constraint;

/**
 * ユーザーの制約値定数。
 *
 * <p>
 * エンティティとバリデーションの双方から参照される。
 * アノテーション属性はコンパイル時定数式しか受け付けないため、
 * static finalフィールドとして定義する。
 */
public final class UserConstraints {

    private UserConstraints() {
    }

    /** ユーザー名の最小文字数。 */
    public static final int USERNAME_MIN_LENGTH = 3;

    /** ユーザー名の最大文字数。 */
    public static final int USERNAME_MAX_LENGTH = 15;

    /**
     * メールアドレスの最大文字数
     * https://errata.rfc-editor.org/eid1690/
     */
    public static final int EMAIL_MAX_LENGTH = 254;

    /** ロール列の最大文字数。ゆとりを持たせた上限。 */
    public static final int ROLE_MAX_LENGTH = 20;

    /**
     * パスワードの最小文字数。
     * 2025年7月の米国国立標準技術研究所（NIST）
     * NIST SP 800-63-4 Digital Identity Guidelines Revision 4では、
     * 単一要素認証では最低15文字、多要素認証に限り最低8文字を推奨している。
     * 今回のプロジェクトは単一要素認証であるが、講義中の利便性のため8文字としている。
     * 本番では非推奨。
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * パスワードの最大文字数。
     * 上記NIST SP 800-63-4 rev4では、少なくとも64文字まで許可するよう推奨している。
     * なお、今回のハッシュ関数はbcryptであるため、そもそも72バイトを超える入力は切り捨てられる点にも注意。
     */
    public static final int PASSWORD_MAX_LENGTH = 64;

    /** パスワードに使用できる半角アルファベット */
    public static final String PASSWORD_LETTER_CHARS = "a-zA-Z";

    /** パスワードに使用できる半角数字 */
    public static final String PASSWORD_DIGIT_CHARS = "0-9";

    /**
     * パスワードに使用できるスペース以外のASCII記号。
     * バックスラッシュはエスケープする必要がある。
     */
    public static final String PASSWORD_SYMBOL_CHARS = "!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~";

    /** パスワード全体で許可する文字を表す正規表現。 */
    public static final String PASSWORD_PATTERN = "["
            + PASSWORD_LETTER_CHARS
            + PASSWORD_DIGIT_CHARS
            + PASSWORD_SYMBOL_CHARS
            + "]+";

    /** ユーザー一覧取得のデフォルト件数。 */
    public static final int DEFAULT_LIST_LIMIT = 50;

    /** ユーザー一覧取得の最小件数。 */
    public static final int MIN_LIST_LIMIT = 1;

    /** ユーザー一覧取得の最大件数。 */
    public static final int MAX_LIST_LIMIT = 100;

}
