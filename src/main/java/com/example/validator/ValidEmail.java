package com.example.validator;

import com.example.constraint.UserConstraints;
import com.example.error.ErrorCode;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import static java.lang.annotation.ElementType.*;
import module java.base;

/**
 * Emailの形式を検証するBean Validationアノテーション。
 * Constraint Compositionで、複数の制約を合成している。
 * 
 * 別途、ビジネスロジックとして、実在性確認や一意性確認を行う必要があるため注意。
 */
@Email(message = ErrorCode.EMAIL_INVALID)
@Size(max = UserConstraints.EMAIL_MAX_LENGTH, message = ErrorCode.EMAIL_TOO_LONG)

@Target({ FIELD, METHOD, PARAMETER, CONSTRUCTOR, TYPE_USE, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidEmail {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
