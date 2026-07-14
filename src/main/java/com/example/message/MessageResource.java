package com.example.message;

import com.example.error.*;
import com.example.message.dto.*;
import com.example.user.Role;

import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.media.*;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.*;
import module java.base;

/**
 * メッセージを扱うリソースクラス。{@code /messages}エンドポイントを提供する。
 */
@Path("/messages")
// 認証済みユーザーに返す動的データのため、古い内容を再利用しないよう指示する。
@Cache(noStore = true) // Cache-Control: no-store
public class MessageResource {

    private final SecurityIdentity identity;
    private final MessageService messageService;

    /**
     * {@code MessageResource}を生成する。
     *
     * @param identity       セキュリティアイデンティティ
     * @param messageService メッセージサービス
     * @throws NullPointerException identityまたはmessageServiceがnullの場合
     */
    public MessageResource(SecurityIdentity identity, MessageService messageService) {
        this.identity = Objects.requireNonNull(identity, "identity must not be null");
        this.messageService = Objects.requireNonNull(messageService, "messageService must not be null");
    }

    /**
     * メッセージを作成する。
     *
     * @param req メッセージの内容を含むリクエスト
     * @return 作成されたメッセージ
     */
    @APIResponse(responseCode = "201", description = "メッセージ作成成功", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MessageResponse.class)))
    @APIResponse(responseCode = "400", description = "バリデーションエラー")
    @APIResponse(responseCode = "401", description = "未認証")
    @POST
    @Authenticated
    public RestResponse<MessageResponse> create(
            @NotNull(message = ErrorCode.REQUEST_BODY_REQUIRED) @Valid MessageRequest req) {
        Long userId = Long.parseLong(identity.getPrincipal().getName());
        var response = messageService.create(userId, req.body());
        Log.debugf("Message created: id=%d, userId=%d", response.id(), userId);
        // 今回はGET /messages/{id}を提供していないため、Locationヘッダーは返さないほうが自然
        return RestResponse.status(Response.Status.CREATED, response);
    }

    /**
     * 最新のメッセージ一覧を取得する。
     *
     * <p>
     * URLに含まれる値（例えば@RestQuery引数）のデシリアライズ失敗に対し、Quarkus RESTはデフォルトで404を返す。
     * しかし、パラメーター不正は400を返したいので、{@code since}は{@code @RestQuery Instant}で直接バインドせず、
     * {@code String}で受けて手動パースする。
     *
     * @param since ISO 8601形式の日時文字列。指定した場合はその日時以降のメッセージを返す。省略した場合は全件対象。
     *              パース不能な値を指定した場合は400。
     * @param limit 最大取得件数（1以上、100以下）。省略した場合は50件。範囲外を指定した場合は400。
     * @return メッセージリスト。createdAtの昇順（同じcreatedAtの場合はidの昇順）
     * @throws InvalidUriParameterException sinceがパース不能な場合
     */
    @APIResponse(responseCode = "200", description = "メッセージリスト取得成功")
    @APIResponse(responseCode = "400", description = "パラメーターが不正")
    @APIResponse(responseCode = "401", description = "未認証")
    @GET
    @Authenticated
    public RestResponse<List<MessageResponse>> list(@BeanParam @Valid MessageListParams params) {
        List<MessageResponse> list = messageService.list(params.since(), params.limit());
        Log.debugf("Message list fetched: count=%d, since=%s, limit=%s", list.size(), params.since(), params.limit());
        return RestResponse.ok(list);
    }

    /**
     * 指定されたIDのメッセージを削除する。
     *
     * @param id 削除するメッセージのID
     * @return 削除成功時は本体なし
     * @throws ResourceNotFoundException   メッセージが存在しない場合
     * @throws ForbiddenOperationException 本人でも admin でもない場合
     */
    @APIResponse(responseCode = "204", description = "削除成功")
    @APIResponse(responseCode = "401", description = "未認証")
    @APIResponse(responseCode = "403", description = "本人でも admin でもない")
    @APIResponse(responseCode = "404", description = "メッセージが存在しない")
    @DELETE
    @Path("/{id}")
    @Authenticated
    public RestResponse<Void> delete(Long id) {
        Long userId = Long.parseLong(identity.getPrincipal().getName());
        boolean isAdmin = identity.hasRole(Role.ADMIN);
        messageService.delete(id, userId, isAdmin);
        Log.infof("Message deleted: id=%d", id);
        return RestResponse.noContent();
    }

}
