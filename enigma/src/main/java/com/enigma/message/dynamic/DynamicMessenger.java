package com.enigma.message.dynamic;


import com.enigma.message.annotations.ThreadMode;
import com.enigma.message.dynamic.util.Log;
import com.enigma.message.dynamic.util.ScanUtil;
import com.enigma.message.handler.EventDispatcher;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tianyang on 17/6/9.
 */
public class DynamicMessenger {

    private Map<Class,List<InvokeMethodInfo>> METHOD_CACHE = new HashMap<>();
    private Map<String, List<ReceiverInfo>> recievers = new HashMap<>();
    private Map<Object,List<ReceiverInfo>> INDEX = new HashMap<>();
    private Map<String,StickyEvent> stickyEventMap = new ConcurrentHashMap<>();
    //通过注解遍历注册类中的属性方法，存到数组中
    private void dealClass(Class cls,Object reciever,Object group,List<ReceiverInfo> indexList){

        List<InvokeMethodInfo> methodInfos = scanMethodInfo(cls);

        for (InvokeMethodInfo method : methodInfos) {
            String msgType = method.getMsgType();
            List<ReceiverInfo> receiverInfos = recievers.get(msgType);
            ReceiverInfo info = new ReceiverInfo(msgType,reciever,group,method.getMethod(),method.getThreadMode(),method.isSticky());
            if (receiverInfos != null)
                receiverInfos.add(info);
            else {
                List<ReceiverInfo> list = new CopyOnWriteArrayList<ReceiverInfo>();
                list.add(info);
                recievers.put(msgType, list);
            }

            indexList.add(info);
        }
    }

    private List<InvokeMethodInfo> scanMethodInfo(Class cls){
        List<InvokeMethodInfo> list = METHOD_CACHE.get(cls);

        if(list != null){
            return list;
        }

        list = ScanUtil.scanMethod(cls);

        METHOD_CACHE.put(cls,list);

        return list;
    }

    public void dump(){
        Log.log("DynamicMessenger",recievers.size()+":"+INDEX.size());
        for (String key : recievers.keySet()) {
            Log.log("DynamicMessenger",key+" left:"+recievers.get(key).size());
        }
    }

    public synchronized void regist(Object reciever) {
        regist(reciever,reciever);
    }

    /**
     *
     * @param reciever
     * @param group 群组
     */
    public synchronized void regist(Object reciever,Object group) {
        if(reciever == null || INDEX.containsKey(reciever))
            return ;

        List<ReceiverInfo> indexList = new ArrayList<>(2);
        INDEX.put(reciever,indexList);


        dealClass(reciever.getClass(),reciever,group,indexList);

        for (ReceiverInfo info : indexList) {
            if(info.sticky){
                StickyEvent stickyEvent = stickyEventMap.get(info.msgType);
                if(stickyEvent != null && (stickyEvent.group == group || stickyEvent.group == null) ){
                    handleSticky(stickyEvent,info);
                }
            }
        }
    }

    public void post(String msgType, Object... objects) {
        handle(msgType,null,objects);
    }

    /**
     *
     * @param group 群组
     * @param msgType 消息类型
     * @param objects 数据列表
     */
    public void postTo(Object group, String msgType, Object... objects) {
        handle(msgType,group,objects);
    }

    public void postSticky(Object group, String msgType, Object... objects){
        StickyEvent stickyEvent = new StickyEvent(msgType,group,objects);
        stickyEventMap.put(msgType,stickyEvent);
        handle(msgType,group,objects);
    }

    public void removeStickyEvent(String msgType){
        stickyEventMap.remove(msgType);
    }

    public synchronized void unRegist(Object reciever) {

        List<ReceiverInfo> indexList = INDEX.remove(reciever);
        if(indexList != null)
        {
            for (ReceiverInfo info : indexList) {
                List<ReceiverInfo> typeRecievers = recievers.get(info.msgType);
                if(typeRecievers != null){
                    typeRecievers.remove(info);
                }
            }
            indexList.clear();
        }

    }

    private void handle(String what, Object group , Object[] objects) {
        List<ReceiverInfo> list = recievers.get(what);
        if (list != null) {
            for (ReceiverInfo receiverInfo : list) {
                if ( group == null || receiverInfo.group == group ) {
                    switch (receiverInfo.threadMode){
                        case POSTING:
                            handleSync(receiverInfo,objects);
                            break;
                        case ASYNC:
                            handleAsync(receiverInfo,objects);
                            break;
                        case MAIN:
                            handleMainThread(receiverInfo,objects);
                            break;
                    }
                }
            }
        }
    }

    private void handleSticky(StickyEvent event,ReceiverInfo receiverInfo){
        switch (receiverInfo.threadMode){
            case POSTING:
                handleSync(receiverInfo,event.objects);
                break;
            case ASYNC:
                handleAsync(receiverInfo,event.objects);
                break;
            case MAIN:
                handleMainThread(receiverInfo,event.objects);
                break;
        }
    }

    private void handleSync(ReceiverInfo receiverInfo,Object[] objects){
        EventDispatcher.getInstance().handleEvent(new InvokeTask(receiverInfo,objects), ThreadMode.POSTING);
    }

    private void handleAsync(ReceiverInfo receiverInfo,Object[] objects){
        EventDispatcher.getInstance().handleEvent(new InvokeTask(receiverInfo,objects),ThreadMode.ASYNC);
    }

    private void handleMainThread(ReceiverInfo receiverInfo,Object[] objects){
        EventDispatcher.getInstance().handleEvent(new InvokeTask(receiverInfo,objects),ThreadMode.MAIN);
    }

    public static class ReceiverInfo implements Serializable {
        Object reciever;
        Object group;
        Method method;
        String msgType;
        boolean sticky;
        ThreadMode threadMode;

        public ReceiverInfo(String msgType,Object reciever,Object group, Method method,ThreadMode threadMode,boolean sticky) {
            this.msgType = msgType;
            this.reciever = reciever;
            this.group = group;
            this.method = method;
            this.threadMode = threadMode;
            this.sticky = sticky;
        }
    }

    class InvokeTask implements Runnable{
        ReceiverInfo receiverInfo;
        Object[] objects;

        public InvokeTask(ReceiverInfo receiverInfo, Object[] objects) {
            this.receiverInfo = receiverInfo;
            this.objects = objects;
        }

        @Override
        public void run() {
            try {
                receiverInfo.method.invoke(receiverInfo.reciever, objects);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class StickyEvent{
        String event;
        Object group;
        Object[] objects;

        public StickyEvent(String event,Object group ,Object[] objects) {
            this.event = event;
            this.group = group;
            this.objects = objects;
        }
    }

    private static volatile DynamicMessenger instance;

    public static DynamicMessenger getInstance(){
        if(instance == null){
            synchronized (DynamicMessenger.class){
                if(instance == null){
                    instance = new DynamicMessenger();
                }
            }
        }
        return instance;
    }
}
