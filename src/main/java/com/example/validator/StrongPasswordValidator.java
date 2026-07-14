package com.example.validator;

import com.example.constraint.UserConstraints;
import jakarta.validation.*;
import module java.base;

/**
 * {@link StrongPassword}アノテーションの実装。パスワードの強度を検証する。
 *
 * <p>
 * 少なくとも1つの半角数字、1つの半角アルファベット、1つの半角記号を含む必要がある。
 * 各文字種は{@link UserConstraints}の定数で定義されたものに限る。
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern DIGIT = Pattern.compile("[" + UserConstraints.PASSWORD_DIGIT_CHARS + "]");
    private static final Pattern LETTER = Pattern.compile("[" + UserConstraints.PASSWORD_LETTER_CHARS + "]");
    private static final Pattern SYMBOL = Pattern.compile("[" + UserConstraints.PASSWORD_SYMBOL_CHARS + "]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true; // null許容は他の制約に委ねる
        }
        boolean hasDigit = DIGIT.matcher(password).find();
        boolean hasLetter = LETTER.matcher(password).find();
        boolean hasSymbol = SYMBOL.matcher(password).find();
        return hasDigit && hasLetter && hasSymbol;
    }
}