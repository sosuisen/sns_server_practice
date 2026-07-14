package com.example.message;

import com.example.constraint.MessageConstraints;
import com.example.message.dto.MessageResponse;
import com.example.user.User;

import io.quarkus.hibernate.orm.panache.*;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import org.hibernate.annotations.*;

import module java.base;

/**
 * メッセージエンティティー。
 *
 * <p>
 * 複数のメッセージが一人の{@link User}（ユーザー）に属する（多対一）。
 * ユーザーが削除された場合、紐づくメッセージもCASCADEで削除される。
 */
@Entity
@Table(name = "messages")
public class Message extends PanacheEntity {

    /** ユーザー。ユーザー削除時にはCASCADEで削除される。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User user;

    /** メッセージ本文 */
    @Column(nullable = false, length = MessageConstraints.BODY_MAX_LENGTH)
    public String body;

    /** メッセージ日時。サービス層で永続化時に設定される。 */
    @Column(name = "created_at", updatable = false, nullable = false)
    public Instant createdAt;

    /**
     * 最新のメッセージをlimit件取得し、createdAt昇順（同値時はid昇順）で返す。
     *
     * <p>
     * 「最新N件」を得るために内部ではcreatedAt降順・id降順で先頭limit件に絞り込み、
     * 昇順に並べ直して返す。
     *
     * @param since nullでない場合は、その日時以降に作成されたメッセージのみを対象とする。
     * @param limit 最大取得件数。
     * @return メッセージレスポンス一覧。createdAt昇順（同値時はid昇順）。
     */
    public static List<MessageResponse> findRecent(Instant since, int limit) {
        var sort = Sort.by("createdAt", "id").descending();
        var query = since == null
                ? findAll(sort)
                : find("createdAt >= ?1", sort, since);
        return query.project(MessageResponse.class)
                .range(0, limit - 1)
                .list()
                .reversed();
    }
}
