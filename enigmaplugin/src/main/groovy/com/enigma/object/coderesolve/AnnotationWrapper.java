package com.enigma.object.coderesolve;


import com.enigma.object.annotations.Async;
import com.enigma.object.annotations.Constructor;

import javax.inject.Named;
import javax.inject.Singleton;

import javassist.CtClass;

/**
 * 类属性上的注解的包装类
 *
 * @author chuansi.wgl on 17/7/18
 */
public class AnnotationWrapper {
    private boolean singletion;
    private String named;
    private boolean inject;
    private String implement;
    private String varibleName;
    private CtClass targetType;
    private String constructor;
    private boolean async;

    public void setAsync(Async async) {
        this.async = null != async;
    }

    public boolean isAsync() {
        return this.async;
    }

    public String getNamed() {
        return named;
    }

    public void setNamed(Named named) {
        if (null != named) {
            this.named = named.value();
        }
    }

    public boolean isSingletion() {
        return singletion;
    }

    public void setSingletion(Singleton singletion) {
        this.singletion = null != singletion;
    }

    public boolean isInject() {
        return inject;
    }

    public void setInject(boolean inject) {
        this.inject = inject;
    }

    public String getImplement() {
        return implement;
    }

    public void setImplement(String implementClazz) {
        this.implement = implementClazz;
    }

    public CtClass getTargetType() {
        return targetType;
    }

    public void setTargetType(CtClass targetType) {
        this.targetType = targetType;
    }

    public String getVaribleName() {
        return varibleName;
    }

    public void setVaribleName(String varibleName) {
        this.varibleName = varibleName;
    }

    public String getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        if (null != constructor) {
            this.constructor = constructor.value();
        }
    }

}
