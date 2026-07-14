package com.example.auth;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.Cache;
import module java.base;

/**
 * 認証エンドポイント。
 */
@Path("/auth")
public class AuthResource {

    private final AuthService authService;

    /**
     * {@code AuthResource} を生成する。
     *
     * @param authService 認証サービス
     * @throws NullPointerException authServiceがnullの場合
     */
    public AuthResource(AuthService authService) {
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
    }

    /**
     * ログインしてJWTを取得する。
     *
     * <p>
     * {@code admin} と {@code user} のどちらのロールでもログインできる。
     *
     * @param req ログインリクエスト
     * @return JWT を含むレスポンス
     */
    @APIResponse(responseCode = "200", description = "ログイン成功")
    @APIResponse(responseCode = "400", description = "バリデーションエラー")
    @APIResponse(responseCode = "401", description = "メールアドレスまたはパスワードが不正")
    @PermitAll
    // トークンは資格情報そのもののため、
    // RFC 6749 5.1 が no-store を要求している。
    @Cache(noStore = true) // Cache-Control: no-store
    @POST
    @Path("/login")
    public RestResponse<TokenResponse> login(@NotNull @Valid LoginRequest req) {
        return RestResponse.ok(authService.login(req));
    }

    /**
     * 管理者としてログインしてJWTを取得する。
     *
     * <p>
     * ユーザーのロールが {@code admin} でない場合は 403 を返す。
     *
     * @param req ログインリクエスト
     * @return JWT を含むレスポンス
     */
    @APIResponse(responseCode = "200", description = "ログイン成功")
    @APIResponse(responseCode = "400", description = "バリデーションエラー")
    @APIResponse(responseCode = "401", description = "メールアドレスまたはパスワードが不正")
    @APIResponse(responseCode = "403", description = "管理者ロールでない")
    @PermitAll
    @Cache(noStore = true) // Cache-Control: no-store
    @POST
    @Path("/admin-login")
    public RestResponse<TokenResponse> adminLogin(@NotNull @Valid LoginRequest req) {
        return RestResponse.ok(authService.loginAdmin(req));
    }
}
