package com.example.startup;

import com.example.user.Role;
import com.example.user.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import module java.base;

/**
 * アプリケーション起動時に初期データを投入するシーダー。
 * ユーザーが0件の場合のみ実行する。
 * devプロファイルではadminとaliceを、prodではadminのみを投入する。
 */
@ApplicationScoped
public class DataSeeder {

    private final LaunchMode launchMode;
    private final Optional<String> prodAdminUsername;
    private final Optional<String> prodAdminEmail;
    private final Optional<String> prodAdminPassword;

    /**
     * {@code DataSeeder}を生成する。
     *
     * @param launchMode        起動モード
     * @param prodAdminUsername prod用初期管理者ユーザー名
     * @param prodAdminEmail    prod用初期管理者メールアドレス
     * @param prodAdminPassword prod用初期管理者パスワード
     * @throws NullPointerException 引数がnullの場合
     */
    public DataSeeder(
            LaunchMode launchMode,
            @ConfigProperty(name = "app.initial-admin.username") Optional<String> prodAdminUsername,
            @ConfigProperty(name = "app.initial-admin.email") Optional<String> prodAdminEmail,
            @ConfigProperty(name = "app.initial-admin.password") Optional<String> prodAdminPassword) {
        this.launchMode = Objects.requireNonNull(launchMode, "launchMode must not be null");
        this.prodAdminUsername = Objects.requireNonNull(prodAdminUsername, "prodAdminUsername must not be null");
        this.prodAdminEmail = Objects.requireNonNull(prodAdminEmail, "prodAdminEmail must not be null");
        this.prodAdminPassword = Objects.requireNonNull(prodAdminPassword, "prodAdminPassword must not be null");
    }

    /**
     * アプリケーション起動イベントを受け取り、初期データを投入する。
     *
     * @param event 起動イベント
     */
    @Transactional
    public void onStart(@Observes StartupEvent event) {
        if (User.count() > 0) {
            return;
        }

        if (launchMode == LaunchMode.DEVELOPMENT || launchMode == LaunchMode.TEST) {
            seedDevUsers();
            return;
        }

        seedProdAdmin();
    }

    private void seedDevUsers() {
        var admin = newUser("admin", "admin@example.com", "pass", Role.admin);
        admin.persist();

        var alice = newUser("alice", "alice@example.com", "pass", Role.user);
        alice.persist();
    }

    private void seedProdAdmin() {
        var config = prodAdminConfig(prodAdminUsername, prodAdminEmail, prodAdminPassword);

        var admin = newUser(config.username(), config.email(), config.password(), Role.admin);
        admin.persist();
    }

    private User newUser(String username, String email, String rawPassword, Role role) {
        var now = Instant.now();
        var user = new User();
        user.username = username;
        user.email = email;
        user.passwordHash = BcryptUtil.bcryptHash(rawPassword);
        user.role = role;
        user.createdAt = now;
        user.modifiedAt = now;
        return user;
    }

    static ProdAdminConfig prodAdminConfig(
            Optional<String> username,
            Optional<String> email,
            Optional<String> password) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(password, "password must not be null");

        return new ProdAdminConfig(
                username.filter(value -> !value.isBlank()).orElse("admin"),
                email.filter(value -> !value.isBlank())
                        .orElseThrow(() -> new IllegalStateException("app.initial-admin.email must be configured")),
                password.filter(value -> !value.isBlank())
                        .orElseThrow(() -> new IllegalStateException("app.initial-admin.password must be configured")));
    }

    record ProdAdminConfig(String username, String email, String password) {
    }
}
