package com.enigma.object.model.info;

/**
 * 构造器结构类
 *
 * @author tianyang on 17/7/14.
 */
public class ConstructorInfo {

    public String constructorId;

    public ConstructorInfo(String constructorId) {
        this.constructorId = constructorId;
    }

    @Override
    public String toString() {
        return "ConstructorInfo{" +
                "constructorId='" + constructorId + '\'' +
                '}';
    }
}
