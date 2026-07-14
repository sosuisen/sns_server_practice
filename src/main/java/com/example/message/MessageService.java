package com.example.message;

import module java.base;
import static com.example.constraint.MessageConstraints.*;

import com.example.error.ForbiddenOperationException;
import com.example.error.ResourceNotFoundException;
import com.example.message.dto.MessageResponse;
import com.example.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * メッセージのユースケースを扱うサービス。
 */
@ApplicationScoped
public class MessageService {
    /**
     * メッセージを作成する。
     *
     * @param userId ユーザーID
     * @param body   メッセージ本文
     * @return 作成されたメッセージのレスポンス
     * @throws NullPointerException  userIdまたはbodyがnullの場合
     * @throws IllegalStateException userIdに対応するユーザーが存在しない場合
     */
    @Transactional
    public MessageResponse create(Long userId, String body) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(body, "body must not be null");

        User user = User.<User>findByIdOptional(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        var message = new Message();
        message.user = user;
        message.body = body;
        message.createdAt = Instant.now();
        message.persist();
        return MessageResponse.from(message);
    }

    /**
     * メッセージを検索する。
     *
     * @param since 指定した場合はその日時以降のメッセージのみ取得する
     * @param limit 最大取得件数。nullの場合はデフォルト件数
     * @return メッセージ一覧。createdAtの昇順（同じcreatedAtの場合はidの昇順）
     * @throws IllegalArgumentException limitが制約に違反する場合
     */
    public List<MessageResponse> list(Instant since, Integer limit) {
        if (limit != null && (limit < MIN_LIST_LIMIT || limit > MAX_LIST_LIMIT)) {
            throw new IllegalArgumentException("limit must be between " + MIN_LIST_LIMIT + " and "
                    + MAX_LIST_LIMIT);
        }
        int effectiveLimit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        return Message.findRecent(since, effectiveLimit);
    }

    /**
     * メッセージを削除する。
     *
     * @param id          削除するメッセージの ID
     * @param requesterId 削除を要求するユーザーの ID
     * @param isAdmin     要求者が admin ロールを持つ場合 {@code true}
     * @throws NullPointerException        id または requesterId が null の場合
     * @throws ResourceNotFoundException   メッセージが存在しない場合
     * @throws ForbiddenOperationException 要求者が投稿者でも admin でもない場合
     */
    @Transactional
    public void delete(Long id, Long requesterId, boolean isAdmin) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(requesterId, "requesterId must not be null");

        Message message = Message.<Message>findByIdOptional(id)
                .orElseThrow(ResourceNotFoundException::new);
        if (!isAdmin && !message.user.id.equals(requesterId)) {
            throw new ForbiddenOperationException();
        }
        message.delete();
    }
}
