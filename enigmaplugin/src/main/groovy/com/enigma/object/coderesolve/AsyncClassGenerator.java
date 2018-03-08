package com.enigma.object.coderesolve;

import com.enigma.object.util.PluginLogger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.CtPrimitiveType;

/**
 * 生成异步对象自动生成代理类
 *
 * @author chuansi.wgl
 */
public class AsyncClassGenerator {
    private static final String ASYNC_CLASS_SUFFIX = "_ASYNC";
    private static final String FILE_TYPE = ".class";
    private ClassPool mClassPool;
    private CtClass mTargetCtClass;
    private CtClass mProxyCtClass;
    private String mProxyFilePath;
    private String mProxySimpleClassName;
    private String mProxyClassName;

    public AsyncClassGenerator(ClassPool classPool, CtClass ctClass, String targetClassFilePath) {
        this.mClassPool = classPool;
        this.mTargetCtClass = ctClass;
        init(targetClassFilePath);
    }


    /**
     * 生成对象异步创建的代理类
     */
    public void generateAsyncClassFile() {
        try {
            mProxyCtClass.setSuperclass(mTargetCtClass);
            CtClass[] ctClasses = new CtClass[1];
            mProxyCtClass.setInterfaces(new CtClass[]{mClassPool.get("com.enigma.object.base.AsyncProxy")});
            this.buildFields()
                    .buildConstructor()
                    .buildInterfaceMethods()
                    .buildMethods()
                    .buildClassFile();

        } catch (Exception e) {
            PluginLogger.getInstance().i("generateAsyncClassFile->" + e.toString());
        }

    }

    private void init(String targetClassFilePath) {
        mProxySimpleClassName = mTargetCtClass.getSimpleName() + ASYNC_CLASS_SUFFIX;
        mProxyClassName = mTargetCtClass.getPackageName() + "." + mProxySimpleClassName;
        this.mProxyFilePath = targetClassFilePath.replace(mTargetCtClass.getName().replace(".", "/") + ".class", "");
        mProxyCtClass = mClassPool.makeClass(mProxyClassName);
    }

    /**
     * 生成构造器
     *
     * @return
     */
    private AsyncClassGenerator buildConstructor() {
        try {
            CtConstructor constructor = new CtConstructor(
                    new CtClass[]{
                            mClassPool.get("com.enigma.object.container.OCenter"),
                            mClassPool.get("java.util.concurrent.ExecutorService"),
                            mClassPool.get("com.enigma.object.model.info.CreateInfo"),
                            mClassPool.get("com.enigma.object.model.info.ConstructorInfo"),
                            mClassPool.get("com.enigma.object.base.AsyncFinishTask")},
                    mProxyCtClass);
            constructor.setModifiers(java.lang.reflect.Modifier.PUBLIC);
            CodeBlock.Builder codeBlock = CodeBlock.builder();
            codeBlock.addStatement("super($L)", "$1")
                    .addStatement("$L.execute(new $T($L,$L,this,$L))",
                            "$2",
                            ClassName.get("com.enigma.object.base", "AsyncRunner"),
                            "$3", "$4", "$5"
                    );
            constructor.setBody("{" + codeBlock.build().toString() + "}");

            mProxyCtClass.addConstructor(constructor);
        } catch (Exception e) {
            PluginLogger.getInstance().i("buildConstructor->", e.toString());
        }

        return this;
    }

    private AsyncClassGenerator buildFields() {
        try {
            CtField target = new CtField(mTargetCtClass, "target", mProxyCtClass);
            target.setModifiers(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.VOLATILE);
            mProxyCtClass.addField(target);

        } catch (Exception e) {
            PluginLogger.getInstance().e("buildFields->" + e.toString());
        }
        return this;

    }

    private AsyncClassGenerator buildInterfaceMethods() {
        try {
            MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder("get");
            getMethodBuilder.addModifiers(Modifier.PUBLIC)
                    .returns(Object.class)
                    .addStatement("return target");

            CtMethod getMethod = CtNewMethod.make(getMethodBuilder.build().toString(), mProxyCtClass);
            mProxyCtClass.addMethod(getMethod);

            MethodSpec.Builder setMethodBuilder = MethodSpec.methodBuilder("set");
            setMethodBuilder.addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(Object.class, "arg")
                    .addStatement("target = ($T)arg", ClassName.get(mTargetCtClass.getPackageName(), mTargetCtClass.getSimpleName()));

            CtMethod setMethod = CtNewMethod.make(setMethodBuilder.build().toString(), mProxyCtClass);
            mProxyCtClass.addMethod(setMethod);
        } catch (Exception e) {
            e.printStackTrace();
            PluginLogger.getInstance().e(e.toString());
        }

        return this;
    }

    /**
     * 生成原对象的所有方法
     *
     * @return
     */
    private AsyncClassGenerator buildMethods() {
        try {
            CtMethod[] tarMethods = mTargetCtClass.getDeclaredMethods();
            for (CtMethod m : tarMethods) {
                if (m.getModifiers() ==
                        java.lang.reflect.Modifier.PUBLIC) {
                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(m.getName());

                    if (m.getModifiers() == java.lang.reflect.Modifier.PUBLIC) {
                        methodBuilder.addModifiers(Modifier.PUBLIC);
                    } else if (m.getModifiers() == java.lang.reflect.Modifier.PROTECTED) {
                        methodBuilder.addModifiers(Modifier.PROTECTED);
                    }
                    CtClass[] params = m.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        CtClass param = params[i];
                        ParameterSpec.Builder paraSpecBuilder = ParameterSpec.builder(ClassName.get(param.getPackageName(), param.getSimpleName()), "arg" + String.valueOf(i));
                        methodBuilder.addParameter(paraSpecBuilder.build());
                    }
                    ClassName returnClassName = null;
                    if (m.getReturnType().isPrimitive()) {
                        Class classParam = Class.forName(((CtPrimitiveType) m.getReturnType()).getWrapperName());
                        classParam = (Class<?>) classParam.getDeclaredField("TYPE").get(classParam);
                        methodBuilder.returns(classParam);
                        if (classParam.equals(void.class)) {
                            returnClassName = ClassName.get(Void.class);
                        }
                    } else {
                        returnClassName = ClassName.get(m.getReturnType().getPackageName(), m.getReturnType().getSimpleName());
                        methodBuilder.returns(returnClassName);
                    }

                    methodBuilder.beginControlFlow("if(target != null)");
                    StringBuilder sb = new StringBuilder();
                    sb.append("target.$L(");

                    for (int i = 0; i < params.length; i++) {
                        sb.append("arg").append(String.valueOf(i)).append(",");
                    }
                    if (params.length > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    sb.append(")");
                    boolean isVoidReturn = true;
                    if (null != returnClassName && returnClassName.compareTo(ClassName.get(Void.class)) == 0) {
                        methodBuilder.addStatement(sb.toString(), m.getName());
                    } else {
                        isVoidReturn = false;
                        methodBuilder.addStatement("return " + sb.toString(), m.getName());
                    }
                    methodBuilder.nextControlFlow("else")
                            .beginControlFlow("synchronized(this)")
                            .beginControlFlow("if(target == null)")
                            .beginControlFlow("try")
                            .addStatement("$T.log(\"APContainerImpl_ASYNC\",\"wait by \"+this.getClass().getName())", ClassName.get("com.enigma.object.util", "Log"))
                            .addStatement("wait()")
                            .nextControlFlow("catch ($T e)", Exception.class)
                            .addStatement("e.printStackTrace()")
                            .endControlFlow()
                            .endControlFlow()
                            .beginControlFlow("if(target != null)");
                    if (!isVoidReturn) {
                        methodBuilder.addStatement("return " + sb.toString(), m.getName());
                    } else {
                        methodBuilder.addStatement(sb.toString(), m.getName());
                    }
                    methodBuilder.endControlFlow()
                            .endControlFlow()
                            .endControlFlow();

                    if (null != returnClassName && returnClassName.compareTo(ClassName.get(Void.class)) == 0) {

                    } else if (m.getReturnType().isPrimitive()) {
                        methodBuilder.addStatement("return 0");
                    } else {
                        methodBuilder.addStatement("return null");
                    }

                    CtMethod method = CtNewMethod.make(methodBuilder.build().toString(), mProxyCtClass);

                    mProxyCtClass.addMethod(method);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            PluginLogger.getInstance().e(e.toString());
        }
        return this;
    }

    /**
     * 生成class文件
     */
    private void buildClassFile() {
        try {
            mProxyCtClass.writeFile(mProxyFilePath);
        } catch (Exception e) {
            PluginLogger.getInstance().e("buildClassFile->" + e.toString());
        }
    }


}
