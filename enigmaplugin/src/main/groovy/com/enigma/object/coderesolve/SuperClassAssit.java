package com.enigma.object.coderesolve;


import com.enigma.object.annotations.Async;
import com.enigma.object.annotations.AutoWrap;
import com.enigma.object.annotations.Constructor;
import com.enigma.object.annotations.Implement;
import com.enigma.object.util.PluginLogger;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

/**
 * 加载父类相关信息
 *
 * @author chuansi.wgl on 8/2/17
 */
public class SuperClassAssit {
    /**
     * 加载父类的成员变量
     * @param cls  当前类
     */
    public static void loadSuperClassMember(CtClass cls, AnnotationContainer annoContainer, List<String> mDummyConstructor) {
        try {
            CtClass suCtClass = cls.getSuperclass();
            if (null != suCtClass) {
                if (null != suCtClass.getAnnotation(AutoWrap.class)) {
                    for (CtField field : suCtClass.getFields() ) {
                        if (field.getModifiers() == (Modifier.PROTECTED|Modifier.PUBLIC)) {
                            if (null != field.getAnnotation(Inject.class)) {
                                AnnotationWrapper wrapper = new AnnotationWrapper();
                                wrapper.setInject(true);
                                wrapper.setImplement(AnnotationAssit.getAssitAnnotationMember(field, Implement.class,"value"));
                                wrapper.setNamed(field.getAnnotation(Named.class) != null? (Named)field.getAnnotation(Named.class):null);
                                wrapper.setSingletion(field.getAnnotation(Singleton.class) !=null? (Singleton) field.getAnnotation(Singleton.class):null);
                                wrapper.setConstructor(field.getAnnotation(Constructor.class) !=null? (Constructor) field.getAnnotation(Constructor.class):null);
                                wrapper.setVaribleName(field.getName());
                                wrapper.setTargetType(field.getType());
                                wrapper.setAsync(field.getAnnotation(Async.class) != null ? (Async) field.getAnnotation(Async.class):null);
                                if(wrapper.isAsync()) {
                                    if( null != wrapper.getImplement()){
                                        if (!mDummyConstructor.contains(wrapper.getImplement())) {
                                            mDummyConstructor.add(wrapper.getImplement());
                                        }
                                    } else {
                                        if (!mDummyConstructor.contains(wrapper.getTargetType().getName())) {
                                            mDummyConstructor.add(wrapper.getTargetType().getName());
                                        }
                                    }
                                }
                                annoContainer.add(wrapper);
                            }

                        }
                    }
                }
                loadSuperClassMember(suCtClass, annoContainer, mDummyConstructor);
            }
        } catch (Exception e) {
            PluginLogger.getInstance().i(e.toString());
        }
    }
}
