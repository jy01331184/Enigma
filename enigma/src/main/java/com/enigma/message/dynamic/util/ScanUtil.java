package com.enigma.message.dynamic.util;

import android.app.Activity;

import com.enigma.message.annotations.Subscribe;
import com.enigma.message.dynamic.InvokeMethodInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyang on 17/7/11.
 */
public class ScanUtil {


    public static List<InvokeMethodInfo> scanMethod(Class cls){

        List<InvokeMethodInfo> list = new ArrayList<>();

        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            Subscribe receiverAnnotation = method.getAnnotation(Subscribe.class);
            if (receiverAnnotation != null) {
                method.setAccessible(true);
                InvokeMethodInfo invokeMethodInfo = new InvokeMethodInfo(method,receiverAnnotation.value(),receiverAnnotation.threadMode(),receiverAnnotation.sticky());
                list.add(invokeMethodInfo);
            }
        }
        if(cls.getSuperclass() != null && cls.getSuperclass() != Object.class && cls.getSuperclass().getClassLoader() != Activity.class.getClassLoader()){
            list.addAll(scanMethod(cls.getSuperclass()));
        }

        return list;
    }
}
