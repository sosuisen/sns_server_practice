package com.example.validator;

import com.example.constraint.MessageConstraints;
import com.example.error.ErrorCode;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import static java.lang.annotation.ElementType.*;
import module java.base;

/**
 * リクエスト本体の形式を検証するBean Validationアノテーション。
 * Constraint Compositionで、複数の制約を合成している。
 */
@NotBlank(message = ErrorCode.BODY_REQUIRED)
@Size(max = MessageConstraints.BODY_MAX_LENGTH, message = ErrorCode.BODY_TOO_LONG)

@Target({ FIELD, METHOD, PARAMETER, CONSTRUCTOR, TYPE_USE, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidBody {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
