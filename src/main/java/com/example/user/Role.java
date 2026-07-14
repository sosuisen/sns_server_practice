package com.example.user;

import java.util.EnumSet;
import java.util.Set;

/**
 * ユーザーのロール。
 *
 * <p>
 * Javaの命名規約ではEnumオブジェクト名は大文字。
 * HibernateやJacksonのデフォルトではEnum定数名がそのままシリアライズされる。
 * 
 * DBのカラム値やJSONでは小文字が優勢であるため、Javaの都合よりはDBやAPIの都合を優先して小文字とした。
 * コンバーターを書くのは複雑になるので避けたい。
 */
public enum Role {
    admin,
    user;

    public static final String ADMIN = "admin";
    public static final String USER = "user";

    static public Set<Role> allRoles() {
        return EnumSet.allOf(Role.class);
    }
}
