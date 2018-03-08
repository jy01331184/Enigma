package com.enigma.object.util;

import android.app.Activity;
import android.text.TextUtils;

import com.enigma.object.annotations.Async;
import com.enigma.object.annotations.AutoWrap;
import com.enigma.object.annotations.Implement;
import com.enigma.object.annotations.OGNL;
import com.enigma.object.model.info.ConstructorInfo;
import com.enigma.object.model.scan.ClassScan;
import com.enigma.object.model.scan.ConstructorParamScan;
import com.enigma.object.model.scan.ConstructorScan;
import com.enigma.object.model.scan.FieldScan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


/**
 * 类扫描工具类
 *
 * @author tianyang on 17/7/14.
 */
public class ScanUtil {

    public static ClassScan scanClass(Class cls) {
        ClassScan classScan = new ClassScan();

        Singleton singleton = (Singleton) cls.getAnnotation(Singleton.class);
        if (singleton != null) {
            classScan.singleton = true;
        }

        AutoWrap autoWrap = (AutoWrap) cls.getAnnotation(AutoWrap.class);
        if (autoWrap != null) {
            classScan.autoWrap = true;
        }
        return classScan;
    }

    /**
     * 扫描类的构造器
     *
     * @param cls             被扫描的类
     * @param constructorInfo
     * @return
     */
    public static ConstructorScan scanConstructor(Class cls, ConstructorInfo constructorInfo) {

        Constructor[] constructors = cls.getDeclaredConstructors();

        if (constructorInfo == null || TextUtils.isEmpty(constructorInfo.constructorId)) {
            ConstructorScan constructorScan = new ConstructorScan();
            for (Constructor constructor : constructors) {
                if (constructor.getParameterTypes().length == 0) {
                    constructorScan.constructor = constructor;
                    constructorScan.parameters = Collections.EMPTY_LIST;
                    constructor.setAccessible(true);
                    return constructorScan;
                }
            }
            if (constructors.length > 0) {
                Constructor constructor = constructors[0];
                constructorScan.constructor = constructor;
                constructor.setAccessible(true);
                constructorScan.parameters = scanConstructorParameters(constructor);
            }
            return constructorScan;
        }

        for (Constructor constructor : constructors) {

            com.enigma.object.annotations.Constructor constructorAnnotation = (com.enigma.object.annotations.Constructor) constructor.getAnnotation(com.enigma.object.annotations.Constructor.class);

            if (constructorAnnotation != null && TextUtils.equals(constructorInfo.constructorId, constructorAnnotation.value())) {

                ConstructorScan constructorScan = new ConstructorScan();
                constructorScan.id = constructorInfo.constructorId;
                constructorScan.constructor = constructor;
                constructor.setAccessible(true);
                constructorScan.parameters = scanConstructorParameters(constructor);

                return constructorScan;
            }
        }

        return null;
    }

    /**
     * 扫描的类的属性
     *
     * @param cls 被扫描的类
     * @return 属性集合
     */
    public static List<FieldScan> scanField(Class cls) {
        List<FieldScan> fieldScanList = new LinkedList<>();
        scanFieldRecursive(cls, fieldScanList);
        return fieldScanList;
    }

    private static void scanFieldRecursive(Class cls, List<FieldScan> scanList) {
        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                FieldScan fieldScan = new FieldScan();
                fieldScan.cls = field.getType();
                fieldScan.field = field;

                Annotation[] annos = field.getDeclaredAnnotations();
                for (Annotation annotation : annos) {
                    Class type = annotation.annotationType();
                    if (type == Named.class) {
                        Named named = (Named) annotation;
                        fieldScan.id = named.value();
                    } else if (type == com.enigma.object.annotations.Constructor.class) {
                        com.enigma.object.annotations.Constructor constructorAnnotation = (com.enigma.object.annotations.Constructor) annotation;
                        fieldScan.constructorInfo = new ConstructorInfo(constructorAnnotation.value());
                    } else if (type == Singleton.class) {
                        fieldScan.singleton = true;
                    } else if (type == Implement.class) {
                        Implement implement = (Implement) annotation;
                        fieldScan.hasImpl = true;
                        fieldScan.cls = implement.value();
                    } else if (type == Async.class) {
                        fieldScan.async = true;
                    } else if (type == OGNL.class) {
                        OGNL ognl = (OGNL) annotation;
                        fieldScan.ognl = ognl.value();
                    }
                }
                scanList.add(fieldScan);
            }
        }

        if (cls.getSuperclass() != null && cls.getSuperclass() != Object.class && cls.getSuperclass().getClassLoader() != Activity.class.getClassLoader()) {
            scanFieldRecursive(cls.getSuperclass(), scanList);
        }
    }

    /**
     * 扫描类的构造器的参数
     *
     * @param constructor 构造器
     * @return
     */
    private static List<ConstructorParamScan> scanConstructorParameters(Constructor constructor) {

        List<ConstructorParamScan> constructorParamScanList = new LinkedList<>();

        Class[] parameterClasses = constructor.getParameterTypes();
        if (parameterClasses != null && parameterClasses.length > 0) {
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for (int i = 0; i < parameterClasses.length; i++) {
                Class cls = parameterClasses[i];
                ConstructorParamScan constructorParamScan = new ConstructorParamScan();
                constructorParamScan.cls = cls;

                if (parameterAnnotations != null && parameterAnnotations.length > i) {
                    Annotation[] annotations = parameterAnnotations[i];
                    if (annotations != null) {
                        for (Annotation annotation : annotations) {
                            Class type = annotation.annotationType();
                            if (type == Named.class) {
                                Named named = (Named) annotation;
                                constructorParamScan.id = named.value();
                            } else if (type == com.enigma.object.annotations.Constructor.class) {
                                com.enigma.object.annotations.Constructor constructorAnnotation = (com.enigma.object.annotations.Constructor) annotation;
                                constructorParamScan.constructorInfo = new ConstructorInfo(constructorAnnotation.value());
                            } else if (type == Singleton.class) {
                                constructorParamScan.singleton = true;
                            } else if (type == Implement.class) {
                                Implement implement = (Implement) annotation;
                                constructorParamScan.hasImpl = true;
                                constructorParamScan.cls = implement.value();
                            } else if (type == OGNL.class) {
                                OGNL ognl = (OGNL) annotation;
                                constructorParamScan.ognl = ognl.value();
                            }
                        }
                    }

                }
                constructorParamScanList.add(constructorParamScan);
            }
        }


        return constructorParamScanList;
    }
}
