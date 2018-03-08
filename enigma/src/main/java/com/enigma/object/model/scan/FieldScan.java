package com.enigma.object.model.scan;


import com.enigma.object.model.info.ConstructorInfo;

import java.lang.reflect.Field;

/**
 * 被注入对象(Field)被扫描后的信息结构体。存放了包括注解在类的各种信息。
 *
 * @author tianyang on 17/7/14.
 */
public class FieldScan {

    public Field field;

    /**
     * 宿主Class
     */
    public Class cls;

    public String id;
    /**
     * 该注入对象的构造信息
     */
    public ConstructorInfo constructorInfo;
    /**
     * 是否为单例模式。
     *
     * @see javax.inject.Singleton
     */
    public boolean singleton;
    /**
     * 是否使用了异步注入方式。
     *
     * @see com.alipay.object.sdk.annotations.Async
     */
    public boolean async;

    public boolean hasImpl;
    /**
     * 该字段(对象)在Ognl上下文中的标志
     */
    public String ognl;

    @Override
    public String toString() {
        return "FieldScan{" +
                "field=" + field +
                ", cls=" + cls +
                ", id='" + id + '\'' +
                ", constructorInfo=" + constructorInfo +
                ", singleton=" + singleton +
                ", async=" + async +
                ", ognl='" + ognl + '\'' +
                '}';
    }
}
