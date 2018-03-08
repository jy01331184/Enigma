package com.enigma.plugin.message;

import com.enigma.message.annotations.Subscribe;
import com.enigma.message.annotations.SubscribeSuperClass;
import com.enigma.message.annotations.ThreadMode;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by tianyang on 17/4/18.
 */
@AutoService(Processor.class)
public class EnigmaProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String genEvent = processingEnv.getOptions().get("slink");
        print("EnigmaProcessor process:"+genEvent);
        if (!Boolean.parseBoolean(genEvent))
            return false;
        HashMap<String, ProxyInfo> proxyMap = getProxyInfoMap(roundEnvironment);
        //遍历proxyMap，并生成代码
        for (String key : proxyMap.keySet()) {
            ProxyInfo proxyInfo = proxyMap.get(key);
            writeCode(proxyInfo);
        }
        return true;
    }


    private void writeCode(ProxyInfo proxyInfo) {
        try {
            Filer filer = processingEnv.getFiler();
            proxyInfo.generateJavaCode().writeTo(filer);
        } catch (IOException e) {
            error(proxyInfo.getTypeElement(),
                    "Unable to write injector for type %s: %s",
                    proxyInfo.getTypeElement(), e.getMessage());
        }
    }

    private HashMap<String, ProxyInfo> getProxyInfoMap(RoundEnvironment roundEnvironment) {

        HashMap<String, ProxyInfo> proxyInfoHashMap = new HashMap<>();


        for (Element element : roundEnvironment.getElementsAnnotatedWith(Subscribe.class)) {

            //target相同只能强转。不同使用getEnclosingElement
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement classElement = (TypeElement) element
                    .getEnclosingElement();
            print(classElement.getQualifiedName().toString());
            PackageElement packageElement = mElementUtils.getPackageOf(classElement);


            String fullClassName = classElement.getQualifiedName().toString();

            String className = classElement.getSimpleName().toString();
            String packageName = packageElement.getQualifiedName().toString();

            ArrayList<SubscribeInfo> subscribeInfos = new ArrayList<>();
            //储存注解信息
            SubscribeInfo subscribeInfo = initSubscribeInfo(executableElement);
            subscribeInfos.add(subscribeInfo);

            //循环遍历父类中注解的方法并加入到其中
            if (proxyInfoHashMap.get(fullClassName) == null) {
                TypeElement superTypeElement;
                while ((superTypeElement = getSuperclassTypeElement(classElement)) != null) {
                    print("superClass:" + classElement.getSuperclass().toString());

                    for (Element elementKey : superTypeElement.getEnclosedElements()) {
                        if (elementKey.getKind() != ElementKind.METHOD) {
                            continue;
                        }
                        final ExecutableElement method = (ExecutableElement) elementKey;
                        SubscribeInfo subscribeInfo1 = initSubscribeInfo(method);
                        if (subscribeInfo1 != null) {
                            subscribeInfos.add(subscribeInfo1);
                        }
                    }
                    classElement = superTypeElement;
                }
            }

            ProxyInfo proxyInfo = proxyInfoHashMap.get(fullClassName);
            if (proxyInfo != null) {
                //说明已经遍历的父类的信息，则只加入获取到的第一个
                proxyInfo.addSubscribeInfo(subscribeInfo);
            } else {
                proxyInfo = new ProxyInfo(packageName, className);
                proxyInfo.setTypeElement(classElement);
                proxyInfo.addSubscribeInfos(subscribeInfos);
                proxyInfoHashMap.put(fullClassName, proxyInfo);
            }

        }
        //该子类无注解Subscribe
        for (Element element : roundEnvironment.getElementsAnnotatedWith(SubscribeSuperClass.class)) {
            //获取继承的注解类
            TypeElement classElement = (TypeElement) element;
            print("class:" + classElement.getQualifiedName().toString());
            PackageElement packageElement = mElementUtils.getPackageOf(classElement);

            String className = classElement.getSimpleName().toString();
            String packageName = packageElement.getQualifiedName().toString();


            String currentClass = classElement.getQualifiedName().toString();
            ProxyInfo proxyInfo;
            //该类为子类，且已有注解
            if (proxyInfoHashMap.get(currentClass) != null) {
                proxyInfo = proxyInfoHashMap.get(currentClass);
            } else {
                proxyInfo = new ProxyInfo(packageName, className);
                proxyInfo.setTypeElement(classElement);
            }


            TypeElement superTypeElement;
            //循环遍历父类中注解的方法并加入到其中
            while ((superTypeElement = getSuperclassTypeElement(classElement)) != null) {
                print("superClass:" + classElement.getSuperclass().toString());
                String superFullclassname = superTypeElement.getQualifiedName().toString();
                ProxyInfo proxyInfoTemp;
                if ((proxyInfoTemp = proxyInfoHashMap.get(superFullclassname)) != null) {
                    proxyInfo.addSubscribeInfos(proxyInfoTemp.getSubscribeInfos());
                    proxyInfoHashMap.put(currentClass, proxyInfo);
                    break;
                }
                classElement = superTypeElement;
            }
        }
        print(proxyInfoHashMap.toString());
        return proxyInfoHashMap;
    }

    private SubscribeInfo initSubscribeInfo(ExecutableElement executableElement) {
        if (executableElement.getAnnotation(Subscribe.class) == null) {
            return null;
        }
        //ProxyInfo 构造
        String methodName = executableElement.getSimpleName().toString();
        List parameters = executableElement.getParameters();

        String eventtype = executableElement.getAnnotation(Subscribe.class).value();
        ThreadMode threadMode = executableElement.getAnnotation(Subscribe.class).threadMode();
        boolean sticky = executableElement.getAnnotation(Subscribe.class).sticky();
        return new SubscribeInfo(methodName, parameters, eventtype, threadMode, sticky);
    }

    private static TypeElement getSuperclassTypeElement(TypeElement element) {
        final TypeMirror superClass = element.getSuperclass();
        //superclass of Object is of NoType which returns some other kind
        if (superClass.getKind() == TypeKind.DECLARED) {
            //F..king Ch...t Have those people used their horrible APIs even once?
            final Element superClassElement = ((DeclaredType) superClass).asElement();
            return (TypeElement) superClassElement;
        } else {
            return null;
        }
    }

    private void print(String message) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(Subscribe.class.getCanonicalName());
        return types;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
