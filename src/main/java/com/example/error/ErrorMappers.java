package com.example.error;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.quarkus.logging.Log;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.*;
import org.hibernate.exception.JDBCConnectionException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import module java.base;

/**
 * 例外をHTTPレスポンスへ変換するマッパー集。
 *
 * <p>
 * Quarkus RESTの{@link ServerExceptionMapper}を使うと、JAX-RS標準の
 * {@code @Provider} + {@code ExceptionMapper<T>}（1例外 = 1クラス）と異なり、
 * 複数のマッパーを1つのクラスにメソッドとしてまとめられる。
 * 対象の例外型はメソッド引数の型から推論され、ビルド時に登録される。
 * 
 * <p>
 * ログレベルはシンプルに、400台のクライアントエラーはDEBUG、500台のサーバーエラーはERRORとする。
 */
public class ErrorMappers {

    // クライアントがエラーメッセージ生成に使う属性のみ公開する。
    private static final Set<String> EXPOSED_ATTRS = Set.of("min", "max", "value");

    /**
     * {@link InvalidUriParameterException}を400 Bad Requestに変換する。
     *
     * <p>
     * Bean Validationで表現できないURIパラメーターの検証（{@code since}のパースなど）の不正を、
     * Bean Validation失敗時と同じ構造化レスポンスで返す。
     */
    @ServerExceptionMapper
    public RestResponse<Object> mapInvalidUriParameter(InvalidUriParameterException e) {
        Log.debugf("Invalid URI parameter [%s] %s", e.code(), e.getMessage());
        return errorResponse(400, e.code());
    }

    /**
     * {@link ResourceNotFoundException}を404 Not Foundに変換する。
     */
    @ServerExceptionMapper
    public RestResponse<Void> mapResourceNotFound(ResourceNotFoundException e) {
        Log.debugf("Resource not found: %s", e.getMessage());
        return RestResponse.status(Response.Status.NOT_FOUND);
    }

    /**
     * {@link InvalidCredentialsException}を401 Unauthorizedに変換する。
     */
    @ServerExceptionMapper
    public RestResponse<Void> mapInvalidCredentials(InvalidCredentialsException e) {
        Log.debugf("Invalid credentials: %s", e.getMessage());
        return RestResponse.ResponseBuilder.<Void>create(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer")
                .build();
    }

    /**
     * {@link ForbiddenOperationException}を403 Forbiddenに変換する。
     */
    @ServerExceptionMapper
    public RestResponse<Void> mapForbiddenOperation(ForbiddenOperationException e) {
        Log.debugf("Operation forbidden: %s", e.getMessage());
        return RestResponse.status(Response.Status.FORBIDDEN);
    }

    /**
     * {@link EmailInUseException}を409 Conflictに変換する。
     */
    @ServerExceptionMapper
    public RestResponse<Object> mapEmailInUse(EmailInUseException e) {
        Log.debugf("Email already in use: %s", e.getMessage());
        return errorResponse(409, ErrorCode.EMAIL_IN_USE);
    }

    /**
     * リクエスト本体の型不一致を400 Bad Requestに変換する。
     *
     * <p>
     * リソースメソッドの引数について、
     * URLに含まれる値のデシリアライズ失敗は404、
     * リクエスト本体のデシリアライズ失敗は400になる。
     * 
     * リクエスト本体については{@link MismatchedInputException}が発生して、
     * Quarkus REST組み込みの{@code BuiltinMismatchedInputExceptionMapper}が
     * 400を返している。
     * 
     * 同じ型に対するこのマッパーで上書きし、他のエラーと同じ{@code errors}構造に揃える。
     */
    @ServerExceptionMapper
    public RestResponse<Object> mapMismatchedInput(MismatchedInputException e) {
        Log.debugf("Request body type mismatch: %s", e.getOriginalMessage());
        return errorResponse(400, ErrorCode.REQUEST_BODY_INVALID);
    }

    /**
     * Bean Validation失敗時のレスポンスを構造化JSONに変換する。
     *
     * <p>
     * レスポンス例:
     *
     * <pre>
     * {
     *   "errors": [
     *     { "code": "USERNAME_INVALID_SIZE", "params": { "min": 3, "max": 15 } }
     *   ]
     * }
     * </pre>
     */
    @ServerExceptionMapper
    public RestResponse<Object> mapConstraintViolation(ConstraintViolationException e) {
        var errors = e.getConstraintViolations().stream()
                .map(v -> {
                    var params = v.getConstraintDescriptor().getAttributes().entrySet().stream()
                            .filter(entry -> EXPOSED_ATTRS.contains(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    return Map.of("code", v.getMessage(), "params", params);
                })
                .toList();
        Log.debugf("Constraint violation: %s", errors);
        return RestResponse.ResponseBuilder.<Object>create(400)
                .entity(Map.of("errors", errors))
                .build();
    }

    /**
     * DB接続エラーを503 Service Unavailableに変換する。
     */
    @ServerExceptionMapper
    public RestResponse<Object> mapJdbcConnection(JDBCConnectionException e) {
        Log.errorf("Database connection error: %s", e.getMessage(), e);
        return errorResponse(503, ErrorCode.DATABASE_UNAVAILABLE);
    }

    /**
     * DB永続化エラーをレスポンスへ変換する。
     *
     * <p>
     * {@code @Transactional}メソッド内で{@link JDBCConnectionException}が発生した場合も、
     * Quarkusのトランザクションマネージャーがロールバック時に{@code PersistenceException}でラップして再スローするため、
     * このマッパーが呼ばれる。
     * 
     * causeを検査して接続エラーを503に変換することで両経路を正しく処理する。
     */
    @ServerExceptionMapper
    public RestResponse<Object> mapPersistence(PersistenceException e) {
        if (e.getCause() instanceof JDBCConnectionException) {
            Log.errorf("Database connection error: %s", e.getCause().getMessage(), e.getCause());
            return errorResponse(503, ErrorCode.DATABASE_UNAVAILABLE);
        }
        Log.errorf("Database persistence error: %s", e.getMessage(), e);
        return errorResponse(500, ErrorCode.DATABASE_ERROR);
    }

    /**
     * {@code {"errors": [{"code": ..., "params": {}}]}}形式のエラーレスポンスを生成する。
     */
    private static RestResponse<Object> errorResponse(int status, String code) {
        return RestResponse.ResponseBuilder.<Object>create(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("errors", List.of(Map.of(
                        "code", code,
                        "params", Map.of()))))
                .build();
    }
}
