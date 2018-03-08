package com.enigma.plugin.message;


import com.enigma.message.annotations.ThreadMode;

import java.util.List;

/**
 * Created by shang on 17/5/4.
 */

public class SubscribeInfo {
    public final List parameters;
    public  String methodName;
    public String eventtype;
    public ThreadMode threadMode = ThreadMode.POSTING;
    public boolean sticky;

    public SubscribeInfo(String methodName, List parameters, String eventtype, ThreadMode threadMode, boolean sticky) {
        this.eventtype = eventtype;
        this.parameters = parameters;
        this.methodName = methodName;
        this.threadMode = threadMode;
        this.sticky = sticky;
    }
}
