package com.enigma.object.base;

/**
 * 当一个对象使用异步方式进行注入时,框架会为原对象生成一个代理对象。
 * 这个代理对象将实现了该接口,通过这个接口的方法可以实现对原对象的操作。
 *
 * @author tianyang on 17/7/24.
 */
public interface AsyncProxy<T> {
    /**
     * 获取实际的原对象。
     */
    T get();

    /**
     * 将异步线程创建完成的对象设置到代理对象上。
     */
    void set(T target);
}
