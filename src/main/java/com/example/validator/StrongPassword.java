package com.example.validator;

import com.example.error.ErrorCode;
import jakarta.validation.*;
import static java.lang.annotation.ElementType.*;
import module java.base;

/**
 * パスワードの形式を検証するConstraint Composition
 */
@Target({ FIELD, METHOD, PARAMETER, CONSTRUCTOR, TYPE_USE, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default ErrorCode.PASSWORD_WEAK;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
