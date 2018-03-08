package com.enigma.message.dynamic;


import com.enigma.message.annotations.ThreadMode;

import java.lang.reflect.Method;

/**
 * Created by tianyang on 17/7/11.
 */
public class InvokeMethodInfo {

    private Method method;
    private String msgType;
    private ThreadMode threadMode;
    private boolean sticky;

    public InvokeMethodInfo(Method method, String msgType, ThreadMode threadMode, boolean sticky) {
        this.method = method;
        this.msgType = msgType;
        this.threadMode = threadMode;
        this.sticky = sticky;
    }

    public Method getMethod() {
        return method;
    }

    public String getMsgType() {
        return msgType;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public boolean isSticky() {
        return sticky;
    }
}
