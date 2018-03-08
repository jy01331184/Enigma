package com.enigma.object.container;

import com.enigma.object.model.info.ConstructorInfo;
import com.enigma.object.model.info.CreateInfo;

/**
 * OCenter依赖注入框架统一接口
 *
 * @author chuansi.wgl on 17/6/21
 */
public abstract class OCenter {

    private static volatile OCenter mInstance;

    /**
     * 获取容器实例
     *
     * @return
     */
    public static OCenter getInstance() {
        if (null == mInstance) {
            synchronized (OCenter.class) {
                if (null == mInstance) {
                    mInstance = new OCenterImpl();
                }
            }
        }
        return mInstance;
    }

    /**
     * 为宿主类自动注入成员对象
     *
     * @param object
     */
    public abstract void wrapField(Object object);

    /**
     * 获取或创建一个对象
     *
     * @param createInfo
     * @param constructorInfo
     * @param <T>
     * @return
     */
    public abstract <T> T getOrCreateObjectById(CreateInfo createInfo, ConstructorInfo constructorInfo);

    /**
     * 获取或创建一个对象
     *
     * @param id
     * @param cls
     * @param constructorInfo
     * @param <T>
     * @return
     */
    public abstract <T> T getOrCreateObjectById(String id, Class<T> cls, ConstructorInfo constructorInfo);

    /**
     * 根据对象在容器中的id获取对象
     *
     * @param id
     * @param <T>
     * @return
     */
    public abstract <T> T getObjectById(String id);

    /**
     * 根据对象在容器中的id移除一个对象
     *
     * @param id
     * @param recursive
     */
    public abstract void removeObjectById(String id, boolean recursive);

    public abstract void bind(Class cls, Class bindCls);

    /**
     * 输出容器信息
     */
    public abstract void dump();

    public abstract void enableAutoWrap();

}
