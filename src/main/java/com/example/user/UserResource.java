package com.example.user;

import com.example.constraint.UserConstraints;
import com.example.error.*;
import com.example.user.dto.*;
import io.quarkus.logging.Log;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.media.*;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.*;
import module java.base;

/**
 * ユーザーを扱うリソースクラス。{@code /users}エンドポイントを提供する。
 */
@Path("/users")
// 認証済みユーザーに返す動的データのため、古い内容を再利用しないよう指示する。
@Cache(noStore = true) // Cache-Control: no-store
public class UserResource {

    private final UserService userService;

    /**
     * {@code UserResource}を生成する。
     *
     * @param userService ユーザーサービス
     * @throws NullPointerException userServiceがnullの場合
     */
    public UserResource(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "userService must not be null");
    }

    /**
     * ユーザーを作成する。
     *
     * @param req ユーザーの内容を含むリクエスト
     * @return 作成されたユーザー
     * @throws EmailInUseException メールアドレスが既に使用されている場合
     */
    @APIResponse(responseCode = "201", description = "ユーザー作成成功", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResponse.class)))
    @APIResponse(responseCode = "400", description = "バリデーションエラー")
    @APIResponse(responseCode = "409", description = "メールアドレスが既に使用中")
    @PermitAll
    @POST
    public RestResponse<UserResponse> create(
            @NotNull(message = ErrorCode.REQUEST_BODY_REQUIRED) @Valid UserRequest req) {
        var response = userService.create(req);
        Log.debugf("User created: id=%d", response.id());
        // 今回はGET /users/{id}を提供していないため、Locationヘッダーは返さないほうが自然
        return RestResponse.status(Response.Status.CREATED, response);
    }

    /**
     * ユーザー一覧を取得する。
     *
     * @param limit 最大取得件数（1以上100以下）。省略した場合は既定の50件。
     * @return ユーザー一覧
     */
    @APIResponse(responseCode = "200", description = "ユーザーリスト取得成功")
    @APIResponse(responseCode = "400", description = "limitが不正")
    @APIResponse(responseCode = "401", description = "未認証")
    @APIResponse(responseCode = "403", description = "adminロール以外")
    @GET
    @RolesAllowed(Role.ADMIN)
    public RestResponse<List<UserResponse>> list(
            @RestQuery @Min(value = UserConstraints.MIN_LIST_LIMIT, message = ErrorCode.LIMIT_INVALID) @Max(value = UserConstraints.MAX_LIST_LIMIT, message = ErrorCode.LIMIT_INVALID) Integer limit) {
        List<UserResponse> list = userService.list(limit);
        Log.debugf("User list fetched: count=%d, limit=%s", list.size(), limit);
        return RestResponse.ok(list);
    }

    /**
     * ユーザー情報を部分更新する。
     *
     * @param id  更新するユーザーのID
     * @param req 更新内容を含むリクエスト
     * @return 更新されたユーザー
     * @throws ResourceNotFoundException ユーザーが存在しない場合
     * @throws EmailInUseException       メールアドレスが既に他ユーザーに使用されている場合
     */
    @APIResponse(responseCode = "200", description = "更新成功")
    @APIResponse(responseCode = "400", description = "バリデーションエラー")
    @APIResponse(responseCode = "401", description = "未認証")
    @APIResponse(responseCode = "403", description = "本人でもadminでもない")
    @APIResponse(responseCode = "404", description = "ユーザーが存在しない")
    @APIResponse(responseCode = "409", description = "メールアドレスが既に使用中")
    @PATCH
    @Path("/{id}")
    @PermissionsAllowed("admin-or-self")
    public RestResponse<UserResponse> patch(Long id,
            @NotNull(message = ErrorCode.REQUEST_BODY_REQUIRED) @Valid PatchUserRequest req) {
        var response = userService.patch(id, req);
        Log.debugf("User patched: id=%d", id);
        return RestResponse.ok(response);
    }

    /**
     * ユーザーを削除する。
     *
     * @param id 削除するユーザーのID
     * @return 削除成功時は本体なし
     * @throws ResourceNotFoundException ユーザーが存在しない場合
     */
    @APIResponse(responseCode = "204", description = "削除成功")
    @APIResponse(responseCode = "401", description = "未認証")
    @APIResponse(responseCode = "403", description = "本人でもadminでもない")
    @APIResponse(responseCode = "404", description = "ユーザーが存在しない")
    @DELETE
    @Path("/{id}")
    @PermissionsAllowed("admin-or-self")
    public RestResponse<Void> delete(Long id) {
        userService.delete(id);
        Log.debugf("User deleted: id=%d", id);
        return RestResponse.noContent();
    }

    /**
     * 指定されたIDのユーザーのロールを変更する。
     *
     * @param id  更新するユーザーのID
     * @param req 変更先のロールを含むリクエスト
     * @return 更新されたユーザー
     * @throws ResourceNotFoundException ユーザーが存在しない場合
     */
    @APIResponse(responseCode = "200", description = "ロール変更成功")
    @APIResponse(responseCode = "400", description = "バリデーションエラー")
    @APIResponse(responseCode = "401", description = "未認証")
    @APIResponse(responseCode = "403", description = "adminロール以外")
    @APIResponse(responseCode = "404", description = "ユーザーが存在しない")
    @PATCH
    @Path("/{id}/role")
    @RolesAllowed(Role.ADMIN)
    public RestResponse<UserResponse> changeRole(Long id,
            @NotNull(message = ErrorCode.REQUEST_BODY_REQUIRED) @Valid RoleRequest req) {
        var response = userService.changeRole(id, req.role());
        Log.debugf("User role changed: id=%d, role=%s", id, req.role());
        return RestResponse.ok(response);
    }

}
