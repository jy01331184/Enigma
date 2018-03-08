package com.enigma.object.coderesolve;

import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * 注解处理辅助类
 *
 * @author chuansi.wgl on 17/7/18
 */
public class AnnotationAssit {

    public static Annotation getAssitAnnotation(CtField field, Class clz) {
        FieldInfo fi = field.getFieldInfo2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute) fi.getAttribute("RuntimeInvisibleAnnotations");
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute) fi.getAttribute("RuntimeVisibleAnnotations");
        Annotation[] anno1;
        if (ainfo == null) {
            anno1 = null;
        } else {
            anno1 = ainfo.getAnnotations();
        }

        Annotation[] anno2;
        if (ainfo2 == null) {
            anno2 = null;
        } else {
            anno2 = ainfo2.getAnnotations();
        }

        String typeName = clz.getName();
        int i;
        if (anno1 != null) {
            for (i = 0; i < anno1.length; ++i) {
                if (anno1[i].getTypeName().equals(typeName)) {
                    return anno1[i];
                }
            }
        }

        if (anno2 != null) {
            for (i = 0; i < anno2.length; ++i) {
                if (anno2[i].getTypeName().equals(typeName)) {
                    return anno2[i];
                }
            }
        }

        return null;
    }

    public static String getAssitAnnotationMember(CtField field, Class clz, String memberName) {
        Annotation annotation = getAssitAnnotation(field, clz);
        if (null != annotation) {
            String valueStr = annotation.getMemberValue(memberName).toString();
            if (valueStr.substring(valueStr.lastIndexOf(".") + 1, valueStr.length()).equals("class")) {
                valueStr = valueStr.substring(0, valueStr.lastIndexOf("."));
            }
            return valueStr;
        }
        return null;
    }
}
