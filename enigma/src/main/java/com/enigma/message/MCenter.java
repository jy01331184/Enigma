package com.enigma.message;


import com.enigma.message.handler.EventDispatcher;
import com.enigma.message.handler.EventHandler;

/**
 * Created by tianyang on 17/6/9.
 */
public abstract class MCenter {

    public abstract void regist(Object o);

    public abstract void regist(Object o, Object groupId);

    public abstract void unRegist(Object o);

    public abstract void post(String event,Object... objects);

    public abstract void postTo(Object group,String event,Object... objects);

    public abstract void postSticky(String event, Object... objects) ;

    public abstract void postStickyTo(Object group,String event, Object... objects) ;

    public abstract void removeStickyEvent(String msgType);

    private static volatile MCenter instance;

    public static MCenter getInstance(){
        if(instance == null){
            synchronized (MCenter.class){
                if(instance == null){
                    instance = new MCenterimpl();
                }
            }
        }
        return instance;
    }


    /**
     * 设置执行在UI线程的事件处理器
     *
     * @param handler
     */
    public void setUIThreadEventHandler(EventHandler handler) {
        EventDispatcher.getInstance().setUIThreadEventHandler(handler);
    }

    /**
     * 设置执行在post线程的事件处理器
     *
     * @param handler
     */
    public void setPostThreadHandler(EventHandler handler) {
        EventDispatcher.getInstance().setPostThreadHandler(handler);
    }

    /**
     * 设置执行在异步线程的事件处理器
     *
     * @param handler
     */
    public void setAsyncEventHandler(EventHandler handler) {
        EventDispatcher.getInstance().setAsyncEventHandler(handler);
    }
}
