package com.enigma.object.coderesolve;


import com.enigma.object.annotations.AutoWrap;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;

/**
 * 注解的包装类
 *
 * @author chuansi.wgl on 17/7/18
 */
public class AnnotationContainer {

    List<AnnotationWrapper> mWrappers;
    CtClass mCtClass;
    String mPackage;
    String clazzName;
    boolean mAutoWrap;

    public boolean isAutoWrap() {
        return mAutoWrap;
    }

    public void setAutoWrap(AutoWrap autoWrap) {
        mAutoWrap = null != autoWrap;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getPackage() {
        return mPackage;
    }

    public void setPackage(String aPackage) {
        mPackage = aPackage;
    }

    public List<AnnotationWrapper> getWrappers() {
        return mWrappers;
    }

    public void setWrappers(List<AnnotationWrapper> wrappers) {
        mWrappers = wrappers;
    }

    public void add(AnnotationWrapper wrapper) {
        this.mWrappers.add(wrapper);
    }

    public AnnotationContainer(CtClass ctClass, String pkg, String clazzName) {
        this.mCtClass = ctClass;
        this.mPackage = pkg;
        this.clazzName = clazzName;
        mWrappers = new ArrayList<>();
    }

    /**
     * 生成代码.
     * @return 生成的代码的字符串
     */
    public String generateStaticLinkCode() {
        return CodeGenerator.getInstance().generateMethod(mWrappers);
    }

    public String generateDynamicLinkCode() {
        return CodeGenerator.getInstance().generateStaticLinkMethodWithOngl(mCtClass, mWrappers, mAutoWrap);

    }


}
