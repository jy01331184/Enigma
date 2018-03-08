package com.enigma.object.base;

/**
 * 当一个类中有异步注入的对象时,宿主类如果关心对象被创建完成的时候,可以实现该接口
 * 来接收被注入对象异步创建完成的回调。
 *
 * @author tianyang on 17/7/21.
 */
public interface AsyncAware {

    /**
     * 异步注入的对象被创建完成时的回调函数。当一个对象使用异步的方式进行注入时,
     * 容器会为原对象生成一个代理对象。对于使用者来说,这个过程是无感知的,这里我们把它笼统的称为"对象"。
     * 但如果使用者关心对象被创建完成的时机,那么他将会在该回调中看到代理对象和实际的原对象。
     *
     * @param proxy  原对象的代理对象
     * @param object 原对象
     * @param id     对象在容器中的ID
     */
    void onLoad(Object proxy, Object object, String id);

}
