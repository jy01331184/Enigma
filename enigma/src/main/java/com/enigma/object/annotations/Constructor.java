package com.enigma.object.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 当一个实现类有多个构造器时,可以使用该注解对构造器进行编号,其value的取值即为构造器的编号。
 * 如果该注解标记在了{@link java.lang.annotation.ElementType.FIELD}上,则表示这个被注入的
 * 对象使用的是哪一个构造器。
 * todo: PARAMETER作用域表示的含义
 *
 * @author tianyang on 17/7/14.
 */
@Target({CONSTRUCTOR, FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Constructor {

    /**
     * 表示构造器的编号或别名。
     */
    String value();

}
