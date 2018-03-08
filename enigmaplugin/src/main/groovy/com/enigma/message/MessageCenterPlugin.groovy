package com.enigma.message

import com.android.build.gradle.TestedExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class MessageCenterPlugin implements Plugin<Project> {

    TestedExtension android;


    @Override
    void apply(Project o) {
        android = o.extensions.findByName("android")
        o.extensions.create('message',MessageExtention.class,o)

        o.afterEvaluate{
            MessageExtention msg = o.extensions.findByName('message')
            println("[MessageCenterPlugin add javac temp dir]")
        }

    }
}