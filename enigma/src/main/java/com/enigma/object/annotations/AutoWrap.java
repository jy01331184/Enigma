package com.enigma.object.annotations;

/**
 * 该注解用于表示一个类里面的被{@link javax.inject.Inject}标记的成员变量将被自动注入。
 * 这里所说的类指的是无法由注入容器创建的类,例如Activity,Service等。这类无法由容器创建的类如果希望
 * 使用容器的依赖注入功能,那么需要使用该注解对其进行标记。
 *
 * @author tianyang on 17/7/18.
 */
public @interface AutoWrap {

    /**
     * 该参数用于表示被注入的对象是否由容器来负责对象的自动释放。
     *
     * @return 如果为ture则由容器负责对象的释放, 否则容器不进行对象的释放处理。
     */
    boolean autoRelease() default true;

}
