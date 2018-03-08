package com.enigma.plugin.message;


import com.enigma.message.annotations.ThreadMode;
import com.enigma.message.slink.Iproxy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by shang on 17/5/4.
 * 存储需要生产的类的基本信息
 * // Generated code from OnceClick. Do not modify!
 * package com.alibaba.shang.apeventbus;
 * <p>
 * import java.lang.ref.WeakReference;
 * import java.util.ArrayList;
 * <p>
 * import com.example.Iproxy;
 * import com.example.module.Event;
 * <p>
 * public class MainActivity_PROXY implements Iproxy {
 * private WeakReference<MainActivity> proxy;
 * private static ArrayList<Integer> eventTypes = new ArrayList<>();
 * <p>
 * static{
 * eventTypes.add(1)
 * }
 *
 * @Override public void init(Object mainActivity) {
 * proxy = new WeakReference<MainActivity>((MainActivity)mainActivity);
 * }
 * <p>
 * public void post(Event event) {
 * switch (event.id) {
 * case 1:
 * proxy.get().showText(event);
 * break;
 * }
 * }
 * @Override public void postSticky(Event event) {
 * <p>
 * }
 * @Override public boolean containsEventId(int id) {
 * return eventTypes.contains(id);
 * }
 * }
 */

public class ProxyInfo {
    private String packageName;
    private String targetClassName;
    private String proxyClassName;
    private TypeElement typeElement;


    public static final String PROXY = "PROXY";
    private ArrayList<SubscribeInfo> subscribeInfos;

    ProxyInfo(String packageName, String className) {
        this.packageName = packageName;
        this.targetClassName = className;
        this.proxyClassName = className + "_" + PROXY;
    }

    String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    JavaFile generateJavaCode() {
        List<FieldSpec> fields = new ArrayList<>();
        FieldSpec fieldProxy = FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(WeakReference.class), ClassName.get(packageName, targetClassName)),
                "proxy",
                Modifier.PRIVATE)
                .build();
        FieldSpec fieldEventTypes = FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
                "eventTypes",
                Modifier.PRIVATE,Modifier.FINAL)
                .initializer("new $T()", ArrayList.class)
                .build();
        FieldSpec fieldStickyEventTypes = FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
                "stickyEventTypes",
                Modifier.PRIVATE,Modifier.FINAL)
                .initializer("new $T()", ArrayList.class)
                .build();
        fields.add(fieldProxy);
        fields.add(fieldEventTypes);
        fields.add(fieldStickyEventTypes);


        TypeSpec typeSpec = TypeSpec.classBuilder(proxyClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Iproxy.class)
                .addFields(fields)
                .addMethod(generateInitMethod())
                .addMethod(generateGetEventIdsMethod())
                .addMethod(generateGetStickyEventIdsMethod())
                .addMethod(generatePostMethod())
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        return javaFile;

    }


    /**
     * @param
     * @Override public void post(Event event){
     * switch (event.id){
     * case 1:
     * proxy.get().showText(event);
     * break;
     * }
     * }
     */
    private MethodSpec generatePostMethod() {
        ArrayList<SubscribeInfo> postInfos = getPostSubscribeInfos(subscribeInfos);
        MethodSpec.Builder postMethodBuilder = MethodSpec.methodBuilder("post")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(String.class, "id")
                .addParameter(Object[].class, "objects", Modifier.FINAL)
                .beginControlFlow("switch (id)");
        for (SubscribeInfo subscribeInfo : postInfos) {
            postMethodBuilder.addCode("case $S:\n", subscribeInfo.eventtype)
                    .addStatement("$T runnable$L = $L",
                            Runnable.class,
                            subscribeInfo.methodName,
                            TypeSpec.anonymousClassBuilder("")
                                    .addSuperinterface(Runnable.class)
                                    .addMethod(MethodSpec.methodBuilder("run")
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .returns(TypeName.VOID)
                                            .addStatement("proxy.get().$L($L)", subscribeInfo.methodName, getParamsStr(subscribeInfo.parameters))
                                            .build())
                                    .build()
                    )
                    .addStatement("$T.getInstance().handleEvent( runnable$L, $T.$L )",
                            ClassName.get("com.enigma.message.handler", "EventDispatcher"),
                            subscribeInfo.methodName,
                            ThreadMode.class,
                            subscribeInfo.threadMode.toString())
                    .addStatement("break");

        }
        postMethodBuilder.endControlFlow();

        return postMethodBuilder.build();


    }

    private String getParamsStr(List<VariableElement> parameters) {
        if (parameters.size() == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        //是list时
        if (parameters.get(0).asType().toString().equals("java.lang.Object[]")) {
            return sb.append("objects").toString();
        }
        //TODO 当objects 的数据类型与返回的值的数据类型不一致时，需要处理,参数的数量也要处理
        for (int i = 0; i < parameters.size() - 1; i++) {
            sb.append("(")
                    .append(parameters.get(i).asType().toString())
                    .append(")")
                    .append(" ")
                    .append("objects[")
                    .append(i)
                    .append("]")
                    .append(",");

        }
        sb.append("(")
                .append(parameters.get(parameters.size() - 1).asType().toString())
                .append(")")
                .append(" ")
                .append("objects[")
                .append(parameters.size() - 1)
                .append("]");
        return sb.toString();
    }

    private ArrayList<SubscribeInfo> getPostSubscribeInfos(ArrayList<SubscribeInfo> subscribeInfos) {
        ArrayList<SubscribeInfo> posts = new ArrayList<>();
        for (SubscribeInfo subscribeInfo : subscribeInfos) {
            if (!subscribeInfo.sticky) {
                posts.add(subscribeInfo);
            }
        }
        return subscribeInfos;
    }

    private MethodSpec generateGetEventIdsMethod() {
        MethodSpec getEventIdsMethod = MethodSpec.methodBuilder("getEventIds")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)))
                .addStatement("return eventTypes")
                .build();
        return getEventIdsMethod;
    }

    private MethodSpec generateGetStickyEventIdsMethod() {
        MethodSpec containsStickyEventIdMethod = MethodSpec.methodBuilder("getStickyEventIds")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)))
                 .addStatement("return stickyEventTypes")
                .build();
        return containsStickyEventIdMethod;
    }

    private MethodSpec generateInitMethod() {
        MethodSpec.Builder initMethodBuilder = MethodSpec.methodBuilder("init")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(Object.class, "mainActivity")
                .addStatement("proxy = new $T<$L>(($L)mainActivity)", WeakReference.class, targetClassName, targetClassName);
        for (SubscribeInfo subscribeInfo : subscribeInfos) {
            initMethodBuilder.addStatement("eventTypes.add($S)", subscribeInfo.eventtype);
            if(subscribeInfo.sticky){
                initMethodBuilder.addStatement("stickyEventTypes.add($S)", subscribeInfo.eventtype);
            }
        }

        return initMethodBuilder.build();

    }

    TypeElement getTypeElement() {
        return typeElement;
    }

    void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public void addSubscribeInfo(SubscribeInfo subscribeInfo) {
        if (subscribeInfos == null) {
            subscribeInfos = new ArrayList<SubscribeInfo>();
        }
        subscribeInfos.add(subscribeInfo);
    }
    public void addSubscribeInfos(ArrayList<SubscribeInfo> subscribeInfo) {
        if (subscribeInfos == null) {
            subscribeInfos = new ArrayList<SubscribeInfo>();
        }
        subscribeInfos.addAll(subscribeInfo);
    }

    public ArrayList<SubscribeInfo> getSubscribeInfos(){
        if(subscribeInfos == null)
            return new ArrayList<SubscribeInfo>();
        return subscribeInfos;
    }
}
