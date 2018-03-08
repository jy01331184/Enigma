package com.enigma.object.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解表明了一个被注入的对象的实现类,这个实现类可以是一个接口的具体实现类,也可以是一个工厂类。
 *
 * @author chuansi.wgl on 17/7/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Implement {
    Class<?> value();
}
