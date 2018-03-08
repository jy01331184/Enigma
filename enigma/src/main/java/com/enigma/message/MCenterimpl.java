package com.enigma.message;


import com.enigma.message.dynamic.DynamicMessenger;
import com.enigma.message.slink.StaticMessenger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tianyang on 17/6/9.
 */
public class MCenterimpl extends MCenter {

    private static boolean USE_DYNAMIC = false;
    private Set<Object> handleInSLink = new HashSet<>();

    private StaticMessenger slink = StaticMessenger.getInstance();
    private DynamicMessenger dynamic = DynamicMessenger.getInstance();

    @Override
    public synchronized void regist(Object o) {
        if(USE_DYNAMIC){
            dynamic.regist(o);
        }
        else if(slink.register(o)){
            handleInSLink.add(o);
        } else {
            dynamic.regist(o);
        }
    }

    @Override
    public synchronized void regist(Object o, Object groupId) {
        if(USE_DYNAMIC){
            dynamic.regist(o,groupId);
        }
        else if(slink.register(o,groupId)){
            handleInSLink.add(o);
        } else {
            dynamic.regist(o,groupId);
        }
    }

    @Override
    public synchronized void unRegist(Object o) {
        if(USE_DYNAMIC){
            dynamic.unRegist(o);
        }
        else if( handleInSLink.remove(o)){
            slink.unregister(o);
        } else {
            dynamic.unRegist(o);
        }
    }

    @Override
    public void post(String event, Object... objects) {
        if(!USE_DYNAMIC){
            slink.post(event,objects);
        }
        dynamic.post(event,objects);
    }

    @Override
    public void postTo(Object group, String event, Object... objects) {
        if(!USE_DYNAMIC){
            slink.postTo(group,event,objects);
        }
        dynamic.postTo(group,event,objects);
    }

    @Override
    public void postSticky(String event, Object... objects) {
        if(!USE_DYNAMIC){
            slink.postSticky(null,event,objects);
        }
        dynamic.postSticky(null,event,objects);
    }

    @Override
    public void postStickyTo(Object group,String event, Object... objects) {
        if(!USE_DYNAMIC){
            slink.postSticky(group,event,objects);
        }
        dynamic.postSticky(group,event,objects);
    }

    public void removeStickyEvent(String msgType){
        if(!USE_DYNAMIC){
            slink.removeStickyEvent(msgType);
        }
        dynamic.removeStickyEvent(msgType);
    }
}
