package com.example.message.dto;

import com.example.message.Message;
import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import module java.base;

/**
 * メッセージレスポンス。
 *
 * <p>
 * Panacheの{@code project()}による射影先としても利用する。
 * {@code userId}・{@code authorName}は関連エンティティーのフィールドのため
 * {@link ProjectedFieldName}でパスを指定する。
 * 
 * 責務がAPI仕様と射影の両方にまたがるため、DTOとしてはやや肥大化している点に注意。
 *
 * <p>
 * 射影ではPanacheがコンストラクターをリフレクションで呼び出すため、
 * nativeイメージ向けに{@link RegisterForReflection}を付与する。
 *
 * @param id         メッセージID
 * @param body       メッセージ本文
 * @param userId     ユーザーのユーザーID
 * @param authorName ユーザー名
 * @param createdAt  メッセージ日時
 */
@RegisterForReflection
public record MessageResponse(
        Long id,
        String body,
        @ProjectedFieldName("user.id") Long userId,
        @ProjectedFieldName("user.username") String authorName,
        Instant createdAt) {
    /**
     * MessageエンティティーからMessageResponseを生成する。
     *
     * @param message Messageエンティティー
     * @return MessageResponse
     */
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.id, message.body,
                message.user.id, message.user.username,
                message.createdAt);
    }
}
