package com.enigma.object.base;

/**
 * 该类包装了异步对象注入完成后的任务。
 *
 * @author tianyang on 17/7/24.
 */
public class AsyncFinishTask {

    /**
     * 关心异步注入回调的对象
     */
    private AsyncAware asyncAware;
    /**
     * 被注入的对象在容器中的Id
     */
    private String id;


    public AsyncFinishTask(AsyncAware asyncAware, String id) {
        this.asyncAware = asyncAware;
        this.id = id;
    }

    /**
     * 通知关心异步任务的对象,表示该异步创建任务已经完成。
     *
     * @param proxy  代理对象
     * @param object 原对象
     */
    public void aware(Object proxy, Object object) {
        asyncAware.onLoad(proxy, object, id);
    }
}
