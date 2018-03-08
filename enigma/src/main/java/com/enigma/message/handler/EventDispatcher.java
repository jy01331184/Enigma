package com.enigma.message.handler;


import com.enigma.message.annotations.ThreadMode;

/**
 * Created by shang[王岩] on 17/6/2
 * fanshang.wy@antfin.com
 */

public class EventDispatcher {
    /**
     * 将接收方法执行在UI线程
     */
    EventHandler mUIThreadEventHandler = new UIThreadEventHandler();

    /**
     * 哪个线程执行的post,接收方法就执行在哪个线程
     */
    EventHandler mPostThreadHandler = new DefaultEventHandler();

    /**
     * 异步线程中执行订阅方法
     */
    EventHandler mAsyncEventHandler = new AsyncEventHandler();

    public void setUIThreadEventHandler(EventHandler handler) {
        mUIThreadEventHandler = handler;
    }

    public void setPostThreadHandler(EventHandler handler) {
        mPostThreadHandler = handler;
    }

    public void setAsyncEventHandler(EventHandler handler) {
        mAsyncEventHandler = handler;
    }

    static class INNER{
        public static EventDispatcher INSTANCE = new EventDispatcher();
    }
    public static EventDispatcher getInstance(){
        return INNER.INSTANCE;
    }

    public void handleEvent(Runnable runnable, ThreadMode threadMode){
        if(threadMode == ThreadMode.POSTING){
            mPostThreadHandler.handleEvent(runnable);
        }else if(threadMode == ThreadMode.ASYNC){
            mAsyncEventHandler.handleEvent(runnable);
        }else if(threadMode == ThreadMode.MAIN){
            mUIThreadEventHandler.handleEvent(runnable);
        }
    }

}
