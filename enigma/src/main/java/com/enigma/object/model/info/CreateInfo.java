package com.enigma.object.model.info;


import com.enigma.object.container.ognl.OgnlContext;

import java.lang.reflect.Field;

/**
 * 被创建的对象所需要的各种信息的包装。
 *
 * @author tianyang on 17/7/21.
 */
public class CreateInfo {

    /**
     * 被创建对象的类型
     */
    public Class cls;
    /**
     * 在容器中的Id
     */
    public String id;
    /**
     * 是否为异步创建
     */
    public boolean async;
    /**
     * 是否为单例对象
     */
    public boolean singleton;
    /**
     * 宿主对象
     */
    public Object host;
    /**
     * 该对象在宿主对象中的作为成员时的变量名
     */
    public String fieldName;
    public Field field;

    public boolean hasImpl;
    public String ognl;
    /**
     * Ognl上下文
     */
    public OgnlContext ognlContext;

    public CreateInfo() {
    }

    public CreateInfo(Class cls, String id, boolean singleton, boolean async) {
        this.cls = cls;
        this.id = id;
        this.singleton = singleton;
        this.async = async;
    }


}
