package com.enigma.object.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 该注解用于表示一个成员对象的注入使用了异步注入方式。
 *
 * @author tianyang on 17/7/21.
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Async {
}
