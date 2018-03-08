package com.enigma.object

import com.android.build.gradle.TestedExtension
import com.enigma.object.annotations.Async
import com.enigma.object.annotations.AutoWrap
import com.enigma.object.annotations.Implement
import com.enigma.object.annotations.Constructor
import com.enigma.object.util.PluginLogger
import javassist.*
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import com.enigma.object.coderesolve.AnnotationContainer
import com.enigma.object.coderesolve.AnnotationWrapper
import com.enigma.object.coderesolve.AsyncClassGenerator
import com.enigma.object.coderesolve.CodeGenerator

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import com.enigma.object.coderesolve.SuperClassAssit
import com.enigma.object.coderesolve.AnnotationAssit

/**
 * 该类对编译完成后的类进行处理
 */
public class InjectTransform {
    List<String> mDummyConstructor;
    Project mProject;
    Boolean mAsyncProxy;

    public InjectTransform(Project project) {
        this.mProject = project;
    }

    public void transformClasses(File inputDir, JavaCompile javaCompile, Boolean asyncProxy) {
        mDummyConstructor = new ArrayList<>();
        ClassPool classes = new ClassPool(true)
        mAsyncProxy = asyncProxy;
        TestedExtension android = mProject.extensions.findByName("android")
        classes.appendClassPath(inputDir.getAbsolutePath())
        android.bootClasspath.each {
            classes.appendClassPath(it.absolutePath)
        }

        javaCompile.classpath.each {
            classes.appendClassPath(it.absolutePath)
        }

        CodeGenerator.getInstance().setClassPool(classes);

        try {
            inputDir.eachFileRecurse {

                if (!it.isDirectory()
                        && it.absolutePath.endsWith(".class")
                        && !it.absolutePath.substring(it.absolutePath.lastIndexOf("/") + 1, it.absolutePath.length()).startsWith("R")) {
                    String path = it.absolutePath.substring(inputDir.absolutePath.length() + 1, it.absolutePath.length() - 6)
                    String clsName = path.replaceAll("/", ".")
                    CtClass ctcls = classes.getCtClass(clsName)
                    if (ctcls.isFrozen()) {
                        ctcls.defrost()
                    }
                    if (ctcls.isAnnotation() || ctcls.isEnum() || ctcls.isInterface() || java.lang.reflect.Modifier.isNative(ctcls.modifiers)) {

                    } else {
                        try {
                            AnnotationContainer annoContainer = new AnnotationContainer(ctcls, ctcls.getPackageName(), ctcls.getSimpleName())
                            annoContainer.setAutoWrap(ctcls.getAnnotation(AutoWrap.class))
                            CtField[] fields = ctcls.getDeclaredFields()
                            boolean shouledInject = false
                            for (CtField field : fields) {
                                if (null != field.getAnnotation(Inject.class)) {
                                    shouledInject = true
                                    AnnotationWrapper wrapper = new AnnotationWrapper()
                                    wrapper.setInject(true)
                                    wrapper.setImplement(AnnotationAssit.getAssitAnnotationMember(field, Implement.class, "value"))
                                    wrapper.setNamed(field.getAnnotation(Named.class))
                                    wrapper.setSingletion(field.getAnnotation(Singleton.class))
                                    wrapper.setConstructor(field.getAnnotation(Constructor.class))
                                    wrapper.setVaribleName(field.getName())
                                    wrapper.setTargetType(field.getType())
                                    wrapper.setAsync(field.getAnnotation(Async.class))
                                    if (wrapper.isAsync()) {
                                        if (null != wrapper.getImplement()) {
                                            if (!mDummyConstructor.contains(wrapper.getImplement())) {
                                                mDummyConstructor.add(wrapper.getImplement());
                                            }
                                        } else {
                                            if (!mDummyConstructor.contains(wrapper.getTargetType().getName())) {
                                                mDummyConstructor.add(wrapper.getTargetType().getName())
                                            }
                                        }
                                    }
                                    annoContainer.add(wrapper)

                                }
                            }
                            SuperClassAssit.loadSuperClassMember(ctcls, annoContainer, mDummyConstructor)
                            if (shouledInject && annoContainer.isAutoWrap()) {
                                CtField __injected = CtField.make("private boolean __injected;", ctcls)
                                ctcls.addField(__injected)
                                CtMethod initMethod = CtNewMethod.make(annoContainer.generateDynamicLinkCode(), ctcls)
                                ctcls.addMethod(initMethod)
                                CtConstructor[] constructors = ctcls.getConstructors()
                                if (null == constructors) {
                                    CtConstructor tempConstructor = CtNewConstructor.defaultConstructor(ctcls)
                                    tempConstructor.setBody("__object_center_init__();")
                                    ctcls.addConstructor(tempConstructor)
                                } else {
                                    for (CtConstructor constructor : constructors) {
                                        constructor.insertBeforeBody("__object_center_init__();")
                                    }
                                }
                                ctcls.writeFile(inputDir.absolutePath)
                            }


                        } catch (NotFoundException e) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            if (!mAsyncProxy) {
                return;
            }

            inputDir.eachFileRecurse {
                if (!it.isDirectory()
                        && it.absolutePath.endsWith(".class")
                        && !it.absolutePath.substring(it.absolutePath.lastIndexOf("/") + 1, it.absolutePath.length()).startsWith("R")) {
                    String path = it.absolutePath.substring(inputDir.absolutePath.length() + 1, it.absolutePath.length() - 6)
                    String clsName = path.replaceAll("/", ".")
                    CtClass ctcls = classes.getCtClass(clsName)
                    if (ctcls.isFrozen()) {
                        ctcls.defrost()
                    }
                    if (ctcls.isAnnotation() || ctcls.isEnum() || ctcls.isInterface() || java.lang.reflect.Modifier.isNative(ctcls.modifiers)) {

                    } else {
                        try {
                            if (mDummyConstructor.contains(ctcls.getName())) {
                                CtClass[] params = new CtClass[1];
                                params[0] = classes.get("com.enigma.object.container.OCenter")
                                CtConstructor emptyConstructor = new CtConstructor(params, ctcls);
                                emptyConstructor.setModifiers(Modifier.PUBLIC);
                                emptyConstructor.setBody("{}");
                                ctcls.addConstructor(emptyConstructor)
                                ctcls.writeFile(inputDir.absolutePath)
                                AsyncClassGenerator asyncClassGenerator = new AsyncClassGenerator(classes, ctcls, it.absolutePath)
                                asyncClassGenerator.generateAsyncClassFile()
                            }

                        } catch (Exception e) {
                            PluginLogger.getInstance().e(e.toString())
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    @Override
    String toString() {
        return 'InjectTransform.groovy'
    }

}