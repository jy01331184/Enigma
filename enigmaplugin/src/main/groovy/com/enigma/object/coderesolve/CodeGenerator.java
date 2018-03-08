package com.enigma.object.coderesolve;


import com.enigma.object.annotations.Constructor;
import com.enigma.object.model.info.ConstructorInfo;
import com.enigma.object.model.info.CreateInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.inject.Named;
import javax.lang.model.element.Modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

/**
 * 代码自动生成总类
 *
 * @author chuansi.wgl
 */
public class CodeGenerator {

    public static final String INIT_METHOD_NAME = "__object_center_init__";
    private static volatile CodeGenerator mInstance;
    ClassPool mClassPool;

    public static CodeGenerator getInstance() {
        if (null == mInstance) {
            synchronized (CodeGenerator.class) {
                if (null == mInstance) {
                    mInstance = new CodeGenerator();
                }
            }
        }
        return mInstance;
    }

    public void setClassPool(ClassPool classPool) {
        this.mClassPool = classPool;
    }

    /**
     * 1. 根据Named/Singleton从容器中取对象, 如果有返回该对象并进入步骤3, 如果没有进入步骤2
     * 2. 根据Implement实例化一个对象(如果没有Implement则使用定义的类型), 交给容器管理, 返回新创建的对象并进入步骤3
     * 3. 使用返回的对象为对应的属性赋值
     *
     * @param wrappers
     * @return
     */
    public String generateMethod(List<AnnotationWrapper> wrappers) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(INIT_METHOD_NAME);
        method.addModifiers(Modifier.PRIVATE)
                .returns(TypeName.VOID);
        for (AnnotationWrapper wrapper : wrappers) {
            method.beginControlFlow("if(__injected)")
                    .addStatement("return")
                    .endControlFlow();
            method.addStatement("__injected = true");
            method.addStatement("$T obj =  $T.getInstance().getObjectById($S)",
                    Object.class,
                    ClassName.get("com.enigma.object.container", "Ocenter"),
                    wrapper.getNamed())
                    .beginControlFlow("if(null != obj)")
                    .addStatement("$L = ($T)obj",
                            wrapper.getVaribleName(),
                            ClassName.get(wrapper.getTargetType().getPackageName(), wrapper.getTargetType().getSimpleName()))
                    .nextControlFlow("else");
//                    .addStatement("$L = new $L()", wrapper.getVaribleName(),wrapper.getImplement())
            createNewObject(method, wrapper);
            method.endControlFlow();
            System.out.println("ObjectCenter U: " + wrapper.getImplement());
        }

        return method.build().toString();
    }

    private void createNewObject(MethodSpec.Builder method, AnnotationWrapper wrapper) {
        try {
            CtClass cls = mClassPool.getCtClass(wrapper.getImplement());
            CtConstructor[] constructors = cls.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                /*
                 * 遍历生成类的构造器
                 * */
                CtConstructor constructor = constructors[i];
                if (null != constructor.getAnnotation(Constructor.class)) {
                    String constructName = ((Constructor) constructor.getAnnotation(Constructor.class)).value();
                    if (wrapper.getConstructor().equals(constructName)) {
                        /*
                        * 如果被注入对象上指定的Constructor名字和实现类的当前构造器上的Name相同,
                        * 则使用当前构造器来构造对象。
                        *
                        * 使用当前构造器构造对象的过程:
                        * Step1. 获取构造器的参数及其类型;
                        * Step2. 非基本类型从容器获取,如果没有则创建; 基本类型直接初始化为默认值;
                        * Step3. 使用参数构造对象
                        * */
                        CtClass[] paramTypes = constructor.getParameterTypes();
                        Object[][] paramAnnos = constructor.getParameterAnnotations();
                        if (null == paramTypes) {
                            /*
                            * 如果构造器上没有参数, 则直接构造对象
                            * */
                            method.addStatement("$L = new $L()", wrapper.getVaribleName(), wrapper.getImplement());

                        } else {
                            /*
                            * 如果构造器上有参数
                            * */
                            StringBuilder sb = new StringBuilder();
                            for (int j = 0; j < paramTypes.length; j++) {
                                /*
                                * 遍历构造器的参数列表
                                * */
                                CtClass paramType = paramTypes[j];
                                if (null != paramAnnos[j]) {
                                    String namedValue = null;
                                    for (int k = 0; k < paramAnnos[j].length; k++) {
                                        /*
                                        * 遍历构造器的参数对应的注解列表
                                        * */
                                        Annotation anno = (Annotation) paramAnnos[j][k];

                                        if (anno instanceof Named) {
                                            namedValue = ((Named) anno).value();
                                            /*
                                            * 根据Named注解的值(namedValue),从对象容器获取对象
                                            * 如果没有获取到对象,则创建
                                            * */
                                        }
                                    }
                                    if (null != namedValue) {
                                        method.addStatement("$T __$L =  $T.getInstance().getObjectById($S)",
                                                Object.class,
                                                paramType.getSimpleName().toLowerCase() + String.valueOf(j),
                                                ClassName.get("com.enigma.object.container", "OCenter"),
                                                namedValue)
                                                .beginControlFlow("if(null == __$L)", paramType.getSimpleName().toLowerCase() + String.valueOf(j))
                                                .addStatement("__$L = new $L()", paramType.getSimpleName().toLowerCase() + String.valueOf(j), paramType.getName())
                                                .endControlFlow();

                                    } else {
                                        method.addStatement("__$L = new $L()", paramType.getSimpleName().toLowerCase() + String.valueOf(j), paramType.getName());
                                    }
                                    method.addStatement("$T __$L =  ($T)__$L",
                                            ClassName.get(paramType.getPackageName(), paramType.getSimpleName()),
                                            paramType.getSimpleName().toLowerCase() + String.valueOf(j),
                                            ClassName.get(paramType.getPackageName(), paramType.getSimpleName()),
                                            paramType.getSimpleName().toLowerCase() + String.valueOf(j));
                                    sb.append("__")
                                            .append(paramType.getSimpleName().toLowerCase())
                                            .append(String.valueOf(j))
                                            .append(",");
                                }
                            }

                            //构造器参数列表构造完毕
                            sb.deleteCharAt(sb.length() - 1);  //去除最后一个','
                            method.addStatement("$L = new $L($L)",
                                    wrapper.getVaribleName(),
                                    wrapper.getImplement(),
                                    sb.toString());
                        }

                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * 1. 根据Named/Singleton从容器中取对象, 如果有返回该对象并进入步骤3, 如果没有进入步骤2
     * 2. 根据Implement实例化一个对象(如果没有Implement则使用定义的类型), 交给容器管理, 返回新创建的对象并进入步骤3
     * 3. 使用返回的对象为对应的属性赋值
     *
     * @param wrappers
     * @return
     */
    public String generateStaicLinkMethod(CtClass ctClass, List<AnnotationWrapper> wrappers, boolean autoWrap) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(INIT_METHOD_NAME);
        method.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.VOID);
        method.beginControlFlow("if(__injected)")
                .addStatement("return")
                .endControlFlow();
        method.addStatement("__injected = true");
        PerformanceMonitor.insertTimeStartPoint(method, "initPerfTimeStart");

        method.addStatement("$T.getInstance().enableAutoWrap()", ClassName.get("com.enigma.object.container", "OCenter"));
        for (AnnotationWrapper wrapper : wrappers) {
            method.addStatement("$T _create_$L = new $T($L.class,$S,$L,$L)",
                    CreateInfo.class,
                    wrapper.getVaribleName(),
                    CreateInfo.class,
                    wrapper.getImplement() == null ? wrapper.getTargetType().getName() : wrapper.getImplement(),
                    wrapper.getNamed(),
                    wrapper.isSingletion(),
                    wrapper.isAsync());
            if (wrapper.isAsync()) {
                method.addStatement("_create_$L.fieldName = $S", wrapper.getVaribleName(), wrapper.getVaribleName())
                        .addStatement("_create_$L.host = this", wrapper.getVaribleName());

            }
            if (null != wrapper.getConstructor()) {
                method.addStatement("$T _construct_$L = new $T($S)",
                        ConstructorInfo.class,
                        wrapper.getVaribleName(),
                        ConstructorInfo.class,
                        wrapper.getConstructor());
                method.addStatement("$L =  ($T)$T.getInstance().getOrCreateObjectById(_create_$L,_construct_$L)",
                        wrapper.getVaribleName(),
                        ClassName.get(wrapper.getTargetType().getPackageName(), wrapper.getTargetType().getSimpleName()),
                        ClassName.get("com.enigma.object.container", "OCenter"),
                        wrapper.getVaribleName(),
                        wrapper.getVaribleName());
            } else {
                method.addStatement("$L =  ($T)$T.getInstance().getOrCreateObjectById(_create_$L,null)",
                        wrapper.getVaribleName(),
                        ClassName.get(wrapper.getTargetType().getPackageName(), wrapper.getTargetType().getSimpleName()),
                        ClassName.get("com.enigma.object.container", "OCenter"),
                        wrapper.getVaribleName());
            }

        }
        PerformanceMonitor.insertTimeEndPointWithLog(method, "initPerfTimeStart");

        return method.build().toString();
    }


    /**
     * 为了解决Ogln上下文的问题,直接使用wrapField
     *
     * @param ctClass
     * @param wrappers
     * @param autoWrap
     * @return
     */
    public String generateStaticLinkMethodWithOngl(CtClass ctClass, List<AnnotationWrapper> wrappers, boolean autoWrap) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(INIT_METHOD_NAME);
        method.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.VOID);
        method.beginControlFlow("if(__injected)")
                .addStatement("return")
                .endControlFlow();
        method.addStatement("__injected = true");
        PerformanceMonitor.insertTimeStartPoint(method, "initPerfTimeStart");
        method.addStatement("$T.getInstance().enableAutoWrap()", ClassName.get("com.enigma.object.container", "OCenter"));
        method.addStatement("$T.getInstance().wrapField(this)", ClassName.get("com.enigma.object.container", "OCenter"));
        PerformanceMonitor.insertTimeEndPointWithLog(method, "initPerfTimeStart");
        return method.build().toString();
    }

}
