package com.enigma.object.model.scan;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 被扫面后的构造器信息结构体
 *
 * @author tianyang on 17/7/14.
 */
public class ConstructorScan {

    public String id;

    /**
     * 构造器
     */
    public Constructor constructor;

    /**
     * 构造器参数
     */
    public List<ConstructorParamScan> parameters;


    @Override
    public String toString() {
        return "ConstructorScan{" +
                "constructorId='" + id + '\'' +
                ", constructor=" + constructor +
                ", parameters=" + parameters +
                '}';
    }
}
