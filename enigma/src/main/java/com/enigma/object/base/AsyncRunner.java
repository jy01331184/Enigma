package com.enigma.object.base;


import com.enigma.object.container.OCenter;
import com.enigma.object.model.info.ConstructorInfo;
import com.enigma.object.model.info.CreateInfo;
import com.enigma.object.util.Log;

/**
 * 当一个注入对象使用异步注入方式时,框架会为当前的注入工作生成一个异步任务。
 *
 * @author chuansi.wgl on 8/3/17
 */
public class AsyncRunner implements Runnable {

    private volatile AsyncProxy mProxy;
    private CreateInfo mCreateInfo;
    private ConstructorInfo mConstructorInfo;
    private AsyncFinishTask mAfterTask;

    public AsyncRunner(CreateInfo createInfo, ConstructorInfo constructorInfo, AsyncProxy proxy, AsyncFinishTask finishTask) {
        this.mProxy = proxy;
        this.mCreateInfo = createInfo;
        this.mConstructorInfo = constructorInfo;
        this.mAfterTask = finishTask;
    }

    @Override
    public void run() {
        synchronized (mProxy) {
            Log.log("OCenterImpl_ASYNC", "start init by " + mProxy.getClass().getName());
            mProxy.set(OCenter.getInstance().getOrCreateObjectById(mCreateInfo, mConstructorInfo));
            Log.log("OCenterImpl_ASYNC", "finish init by " + mProxy.getClass().getName() + "->" + mProxy.get().getClass().getName());
            mProxy.notifyAll();
            if (mAfterTask != null) {
                mAfterTask.aware(mProxy, mProxy.get());
            }
        }
    }
}
