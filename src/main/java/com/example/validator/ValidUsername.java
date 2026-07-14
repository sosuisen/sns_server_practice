package com.example.validator;

import com.example.constraint.UserConstraints;
import com.example.error.ErrorCode;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import static java.lang.annotation.ElementType.*;
import module java.base;

/**
 * ユーザー名の形式を検証するBean Validationアノテーション。
 * Constraint Compositionで、複数の制約を合成している。
 * 
 * 別途、ビジネスロジックとして、一意性確認が必要な場合があるため注意。
 */
@Size(min = UserConstraints.USERNAME_MIN_LENGTH, max = UserConstraints.USERNAME_MAX_LENGTH, message = ErrorCode.USERNAME_INVALID_SIZE)
@Pattern(regexp = "[a-zA-Z0-9_]+", message = ErrorCode.USERNAME_INVALID_CHARS)

@Target({ FIELD, METHOD, PARAMETER, CONSTRUCTOR, TYPE_USE, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidUsername {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
