package com.example.auth;

import module java.base;
import com.example.error.ForbiddenOperationException;
import com.example.error.InvalidCredentialsException;
import com.example.user.Role;
import com.example.user.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.logging.Log;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * 認証のユースケースを扱うサービス。
 */
@ApplicationScoped
public class AuthService {
    // タイミングサイドチャネル（ユーザー列挙）対策用のダミーハッシュ。
    // ユーザー不在時もこのハッシュと bcrypt 照合を行い、応答時間を実在ユーザー時と揃える。
    private static final String DUMMY_PASSWORD_HASH = BcryptUtil.bcryptHash("invalid dummy password");

    private final String issuer;
    private final Duration tokenValidity;

    /**
     * {@code AuthService} を生成する。
     *
     * @param issuer        JWTのissuer
     * @param tokenValidity JWTの有効期間
     * @throws NullPointerException issuerがnullの場合
     */
    public AuthService(
            @ConfigProperty(name = "mp.jwt.verify.issuer") String issuer,
            @ConfigProperty(name = "app.jwt.token-validity", defaultValue = "24h") Duration tokenValidity) {
        this.issuer = Objects.requireNonNull(issuer, "issuer must not be null");
        this.tokenValidity = tokenValidity;
    }

    /**
     * メールアドレスとパスワードで認証し、JWTを発行する。
     *
     * <p>
     * ロールによる制限はなく、{@code admin} と {@code user} のどちらでもログインできる。
     *
     * @param req ログインリクエスト
     * @return 発行されたJWTを含むレスポンス
     * @throws NullPointerException        reqがnullの場合
     * @throws InvalidCredentialsException メールアドレスまたはパスワードが正しくない場合
     */
    public TokenResponse login(LoginRequest req) {
        Objects.requireNonNull(req, "req must not be null");
        return authenticateAndIssue(req, Role.allRoles());
    }

    /**
     * メールアドレスとパスワードで認証し、管理者専用のJWTを発行する。
     *
     * <p>
     * ユーザーのロールが {@code admin} でない場合は認可しない。
     *
     * @param req ログインリクエスト
     * @return 発行されたJWTを含むレスポンス
     * @throws NullPointerException        reqがnullの場合
     * @throws InvalidCredentialsException メールアドレスまたはパスワードが正しくない場合
     * @throws ForbiddenOperationException ユーザーのロールが {@code admin} でない場合
     */
    public TokenResponse loginAdmin(LoginRequest req) {
        Objects.requireNonNull(req, "req must not be null");
        return authenticateAndIssue(req, Set.of(Role.admin));
    }

    private TokenResponse authenticateAndIssue(LoginRequest req, Set<Role> allowedRoles) {
        var user = authenticate(req.email(), req.password());
        verifyRoleAllowed(user, allowedRoles);
        var token = issueToken(user);

        Log.infof("Login success: userId=%d, role=%s", user.id, user.role);
        return new TokenResponse(token);
    }

    private User authenticate(String email, String password) {
        var user = User.findByEmail(email);
        var passwordHash = (user != null) ? user.passwordHash : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = BcryptUtil.matches(password, passwordHash);
        if (user == null || !passwordMatches) {
            Log.warn("Login failed (invalid credentials)");
            throw new InvalidCredentialsException();
        }
        return user;
    }

    private void verifyRoleAllowed(User user, Set<Role> allowedRoles) {
        if (!allowedRoles.contains(user.role)) {
            Log.warnf("Login denied (role not allowed): userId=%d, role=%s", user.id, user.role);
            throw new ForbiddenOperationException();
        }
    }

    private String issueToken(User user) {
        return Jwt.issuer(issuer)
                .subject(String.valueOf(user.id))
                .claim("username", user.username)
                .claim("email", user.email)
                .groups(Set.of(user.role.name()))
                .expiresIn(tokenValidity)
                .sign();
    }
}
