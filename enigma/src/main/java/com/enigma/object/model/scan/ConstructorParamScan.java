package com.enigma.object.model.scan;


import com.enigma.object.model.info.ConstructorInfo;

/**
 * 被框架扫描出的构造器参数结构体
 *
 * @author tianyang on 17/7/14.
 */
public class ConstructorParamScan {

    /**
     * 参数的类型
     */
    public Class cls;

    public String id;

    public ConstructorInfo constructorInfo;

    public boolean singleton;

    public String ognl;

    public boolean hasImpl;

    @Override
    public String toString() {
        return "ConstructorParamScan{" +
                "cls=" + cls +
                ", id='" + id + '\'' +
                ", constructorInfo=" + constructorInfo +
                ", singleton=" + singleton +
                ", ognl='" + ognl + '\'' +
                '}';
    }
}
