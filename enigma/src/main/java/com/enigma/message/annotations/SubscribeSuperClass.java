package com.enigma.message.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by shang[王岩] on 17/6/12
 * fanshang.wy@antfin.com
 *
 * 在父类中有方法 注解@Subscribe，而子类中无注解@Subscribe，需要在子类的头部使用该注解
 * <example>
 *     public class Father{
 *         @Subscribe
 *         public void showMessage(){};
 *     }
 *     //use
 *     @SubscribeSuperClass
 *     public class Child1 extends Father{
 *         public void show(){};
 *     }
 *     //no use
 *     public class Child2 extends Father{
 *         @Surscribe
 *         public void show(){};
 *     }
 * </>
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeSuperClass {
    Class<?> clazz() default Object.class;
}
