package com.enigma.object

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import com.enigma.object.model.ObjectExtentsion

/**
 * 对象容器中心插件, 该插件用于自动生成相关代码, 异步创建对象代理的生成等。
 *
 * @author chuansi.wgl on 17/7/21
 */
public class OCenterPlugin implements Plugin<Project> {

    TestedExtension android;

    @Override
    void apply(Project project) {
        android = project.extensions.findByName("android")
        project.configurations.create("objectsdk")
        project.extensions.create('obj', ObjectExtentsion.class)
        project.afterEvaluate {
            hookCompile(project)
        }
    }

    void hookCompile(Project project) {

        if (android instanceof AppExtension) {
            android.applicationVariants.each {
                JavaCompile javaCompile = it.javaCompiler
                javaCompile.doLast {
                    javaCompile.outputs.files.files.each {
                        if (it.absolutePath.endsWith("/classes/" + it.name)) {
                            System.out.println(project.name + "hookCompile:" + it.absolutePath)
                            ObjectExtentsion objectExtentsion = project.extensions.findByName('obj')
                            boolean asyncProxy = objectExtentsion == null ? false : objectExtentsion.asyncProxy
                            InjectTransform injectTransform = new InjectTransform(project)
                            injectTransform.transformClasses(it, javaCompile, asyncProxy)
                        }
                    }
                }
            }
        } else if (android instanceof LibraryExtension) {
            android.libraryVariants.each {
                JavaCompile javaCompile = it.javaCompiler
                javaCompile.doLast {
                    javaCompile.outputs.files.files.each {
                        if (it.absolutePath.endsWith("/classes/" + it.name)) {
                            System.out.println(project.name + "hookCompile:" + it.absolutePath)
                            ObjectExtentsion objectExtentsion = project.extensions.findByName('obj')
                            boolean asyncProxy = objectExtentsion == null ? false : objectExtentsion.asyncProxy
                            InjectTransform injectTransform = new InjectTransform(project)
                            injectTransform.transformClasses(it, javaCompile, asyncProxy)
                        }
                    }
                }
            }
        }
    }

}