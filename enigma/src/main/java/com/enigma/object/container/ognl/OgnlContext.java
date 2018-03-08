package com.enigma.object.container.ognl;

/**
 * Ognl上下文接口。
 *
 * @author tianyang on 17/7/28.
 */
public interface OgnlContext {

    /**
     * 在Ognl上下文中查找一个对象并返回。对象的查找会首先递归的在父Ognl上下文中查找,
     * 如果父Ognl上下文中为找到对应对象,则在当前上下文中查找。
     *
     * @param ognl 查找的对象
     * @param <T>
     * @return 在上下文中找到的对象, 如果没有找到对象, 则返回为空
     */
    <T> T get(String ognl);

    /**
     * 为当前上下文设置一个父上下文。
     *
     * @param parent 父OgnlContext
     */
    void setParent(OgnlContext parent);
}
