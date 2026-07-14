package com.example.user;

import static com.example.constraint.UserConstraints.*;
import com.example.error.EmailInUseException;
import com.example.user.dto.UserResponse;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import module java.base;
import org.hibernate.exception.ConstraintViolationException;

/**
 * ユーザーエンティティ。
 */
@Entity
// UNIQUE制約に名前を付け、emailの重複による例外を識別できるようにする。
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = User.UK_USERS_EMAIL, columnNames = "email"))
public class User extends PanacheEntity {

    /** emailのUNIQUE制約名。違反検出時にemailの重複と識別するために用いる。 */
    public static final String UK_USERS_EMAIL = "uk_users_email";

    @Column(nullable = false, length = USERNAME_MAX_LENGTH)
    public String username;

    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    // Role enumにより型レベルで許可値を保証する。
    // @Enumerated(EnumType.STRING)のためHibernateがスキーマ生成時に
    // CHECK (role in ('admin','user'))のように自動出力する。
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = ROLE_MAX_LENGTH)
    public Role role = Role.user;

    @Column(name = "created_at", updatable = false, nullable = false)
    public Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    public Instant modifiedAt;

    /**
     * メールアドレスを正規化（小文字化）する。
     *
     * <p>
     * 大文字小文字違い（例{@code Admin@…}と{@code admin@…}）を同一アドレスとして扱うため、
     * 保存・検索の両方でこの正規形を用いる。locale依存を避けるため{@link Locale#ROOT}を使う。
     *
     * @param email メールアドレス。{@code null}可
     * @return 正規化済みメールアドレス。{@code null}の場合は{@code null}
     */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    /**
     * メールアドレスでユーザーを検索する。大文字小文字は区別しない。
     *
     * @param email メールアドレス
     * @return 該当ユーザー。存在しない場合は{@code null}
     */
    public static User findByEmail(String email) {
        return find("email", normalizeEmail(email)).firstResult();
    }

    /**
     * 指定したメールアドレスを持つユーザーが存在するかを判定する。
     * 大文字小文字は区別しない。
     *
     * @param email メールアドレス
     * @return 存在する場合は{@code true}、存在しない場合は{@code false}
     */
    public static boolean existsByEmail(String email) {
        return count("email", normalizeEmail(email)) > 0;
    }

    /**
     * 指定したメールアドレスを持つユーザーが存在するかを判定する。
     * ただし、指定したIDのユーザーは除外する。
     * 大文字小文字は区別しない。
     *
     * @param email メールアドレス
     * @param id    除外するユーザーID
     * @return 存在する場合は{@code true}、存在しない場合は{@code false}
     */
    public static boolean existsByEmailExcludingId(String email, Long id) {
        return count("email = ?1 and id != ?2", normalizeEmail(email), id) > 0;
    }

    /**
     * ユーザー一覧をid昇順でlimit件まで取得する。
     *
     * @param limit 最大取得件数
     * @return ユーザーレスポンス一覧。id昇順・先頭limit件
     */
    public static List<UserResponse> findAllSorted(int limit) {
        return findAll(Sort.by("id")).project(UserResponse.class).range(0, limit - 1).list();
    }

    /**
     * このユーザーを永続化・flushし、emailのUNIQUE制約違反を{@link EmailInUseException}に変換する。
     *
     * <p>
     * 重複を事前チェック（{@link #countByEmail}等）しても、TOCTOU（check-then-actの競合）は防げない。
     * その場合はDBのUNIQUE制約{@value #UK_USERS_EMAIL}で防ぎ、例外を{@link EmailInUseException}に翻訳する。
     *
     * <p>
     * 明示的にflushしない場合、INSERTが実行されるのはトランザクションのコミット時となり、
     * 呼び出し側のメソッド内でUNIQUE制約違反を捕捉できない。そのためここでflushして違反を捕捉する。
     *
     * @throws EmailInUseException emailが既に使用されている場合
     */
    public void persistUniqueEmail() {
        try {
            persistAndFlush();
        } catch (PersistenceException e) {
            if (isUniqueEmailViolation(e)) {
                throw new EmailInUseException();
            }
            throw e;
        }
    }

    /**
     * 例外のcauseのチェインにemailのUNIQUE制約（{@value #UK_USERS_EMAIL}）違反が含まれるか判定する。
     *
     * @param e 検査する例外
     * @return emailのUNIQUE制約違反の場合true
     */
    private static boolean isUniqueEmailViolation(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                return UK_USERS_EMAIL.equalsIgnoreCase(cve.getConstraintName());
            }
        }
        return false;
    }
}
