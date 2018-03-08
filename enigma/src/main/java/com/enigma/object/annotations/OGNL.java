package com.enigma.object.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于表示获取一个上下文中的对象。
 * todo: 这里需要更详细的解释ONGL的概念
 *
 * @author tianyang on 17/7/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface OGNL {

    public String value();

    String THIS = "#this";
}
