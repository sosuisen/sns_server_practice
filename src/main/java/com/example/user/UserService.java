package com.example.user;

import module java.base;
import static com.example.constraint.UserConstraints.*;
import com.example.error.*;
import com.example.user.dto.*;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * ユーザーのユースケースを扱うサービス。
 */
@ApplicationScoped
public class UserService {

    /**
     * ユーザーを登録する。
     *
     * @param req ユーザー登録リクエスト
     * @return 登録されたユーザーのレスポンス
     * @throws NullPointerException reqがnullの場合
     * @throws EmailInUseException  emailが既に使用されている場合
     */
    @Transactional
    public UserResponse create(UserRequest req) {
        Objects.requireNonNull(req, "req must not be null");

        var email = User.normalizeEmail(req.email());
        validateEmailNotUsed(email);

        var now = Instant.now();
        var user = new User();
        user.username = req.username();
        user.email = email;
        user.passwordHash = BcryptUtil.bcryptHash(req.password());
        user.createdAt = now;
        user.modifiedAt = now;
        user.persistUniqueEmail();
        return UserResponse.from(user);
    }

    /**
     * ユーザーを取得する。
     *
     * @param limit 最大取得件数。nullの場合はデフォルト件数
     * @return ユーザー一覧。idの昇順
     * @throws IllegalArgumentException limitが制約に違反する場合
     */
    public List<UserResponse> list(Integer limit) {
        if (limit != null && (limit < MIN_LIST_LIMIT || limit > MAX_LIST_LIMIT)) {
            throw new IllegalArgumentException("limit must be between " + MIN_LIST_LIMIT + " and "
                    + MAX_LIST_LIMIT);
        }
        int effectiveLimit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        return User.findAllSorted(effectiveLimit);
    }

    /**
     * ユーザーを部分更新する。
     *
     * @param id  ユーザーID
     * @param req ユーザー部分更新リクエスト
     * @return 更新されたユーザーのレスポンス
     * @throws NullPointerException      idまたはreqがnullの場合
     * @throws ResourceNotFoundException 対象ユーザーが存在しない場合
     * @throws EmailInUseException       emailが他ユーザーに使用されている場合
     */
    @Transactional
    public UserResponse patch(Long id, PatchUserRequest req) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(req, "req must not be null");

        User user = User.<User>findByIdOptional(id)
                .orElseThrow(ResourceNotFoundException::new);
        validateEmailNotUsedByOthers(User.normalizeEmail(req.email()), id);
        applyPatch(user, req);
        user.modifiedAt = Instant.now();
        user.persistUniqueEmail();
        return UserResponse.from(user);
    }

    /**
     * ユーザーを削除する。
     *
     * @param id ユーザーID
     * @throws NullPointerException      idがnullの場合
     * @throws ResourceNotFoundException 対象ユーザーが存在しない場合
     */
    @Transactional
    public void delete(Long id) {
        Objects.requireNonNull(id, "id must not be null");

        if (!User.deleteById(id)) {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * ユーザーのロールを変更する。
     *
     * @param id   ユーザーID
     * @param role ロール
     * @return 更新されたユーザーのレスポンス
     * @throws NullPointerException      idまたはroleがnullの場合
     * @throws ResourceNotFoundException 対象ユーザーが存在しない場合
     */
    @Transactional
    public UserResponse changeRole(Long id, Role role) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(role, "role must not be null");

        User user = User.<User>findByIdOptional(id)
                .orElseThrow(ResourceNotFoundException::new);
        user.role = role;
        user.modifiedAt = Instant.now();
        return UserResponse.from(user);
    }

    private void validateEmailNotUsed(String email) {
        if (User.existsByEmail(email)) {
            throw new EmailInUseException();
        }
    }

    private void validateEmailNotUsedByOthers(String email, Long myId) {
        if (User.existsByEmailExcludingId(email, myId)) {
            throw new EmailInUseException();
        }
    }

    private void applyPatch(User user, PatchUserRequest req) {
        if (req.username() != null) {
            user.username = req.username();
        }
        if (req.email() != null) {
            user.email = User.normalizeEmail(req.email());
        }
        if (req.password() != null) {
            user.passwordHash = BcryptUtil.bcryptHash(req.password());
        }
    }
}
