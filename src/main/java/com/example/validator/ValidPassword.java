package com.example.validator;

import com.example.constraint.UserConstraints;
import com.example.error.ErrorCode;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import static java.lang.annotation.ElementType.*;
import module java.base;

/**
 * パスワードの形式を検証するBean Validationアノテーション。
 * Constraint Compositionで、複数の制約を合成している。
 */
@Size(min = UserConstraints.PASSWORD_MIN_LENGTH, max = UserConstraints.PASSWORD_MAX_LENGTH, message = ErrorCode.PASSWORD_INVALID_SIZE)
@Pattern(regexp = UserConstraints.PASSWORD_PATTERN, message = ErrorCode.PASSWORD_INVALID_CHARS)
@StrongPassword(message = ErrorCode.PASSWORD_WEAK)

@Target({ FIELD, METHOD, PARAMETER, CONSTRUCTOR, TYPE_USE, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidPassword {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
