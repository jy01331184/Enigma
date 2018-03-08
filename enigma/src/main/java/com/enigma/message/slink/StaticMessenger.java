package com.enigma.message.slink;

import android.text.TextUtils;

import com.enigma.message.dynamic.util.Log;
import com.enigma.message.slink.exception.InvalidParamException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class StaticMessenger {


    private final Map<String, List<ProxyWrapper>> mProxysMap;
    private final Map<Object, ProxyWrapper> mIndexProxyMap;
    private final Map<String, StickyEvent> mStickyEventMap;
    private final Map<Class, Class> CLASS_CACHE;

    static class EventBusHolder {
        public static StaticMessenger instance = new StaticMessenger();
    }

    private StaticMessenger() {
        mProxysMap = new HashMap<>();
        mStickyEventMap = new ConcurrentHashMap<>();
        mIndexProxyMap = new HashMap<>();
        CLASS_CACHE = new HashMap<>();
    }

    public static StaticMessenger getInstance() {
        return EventBusHolder.instance;
    }

    public void dump() {
        Log.log("APEventBus", mProxysMap.size() + ":" + mIndexProxyMap.size());
        for (String key : mProxysMap.keySet()) {
            Log.log("APEventBus", key + " left:" + mProxysMap.get(key).size());
        }
    }

    public synchronized boolean register(Object o, Object groupId) {

        if (mIndexProxyMap.containsKey(o)) {
            return true;
        }
        //根据该类实例化apt生成的代理类对象
        try {
            Class cls = CLASS_CACHE.get(o.getClass());
            if (cls == null) {
                cls = Class.forName(o.getClass().getCanonicalName() + "_PROXY");
                CLASS_CACHE.put(o.getClass(), cls);
            }
            Iproxy iproxy = (Iproxy) cls.newInstance();

            ProxyWrapper proxyWrapper = initProxysMap(o, groupId, iproxy);
            processStickyEvent(proxyWrapper);
        } catch (Exception e) {
            return false;
        }


        return true;
    }

    private void processStickyEvent(ProxyWrapper proxyWrapper) {
        if (proxyWrapper == null) {
            return;
        }

        List<String> stickyEvents = proxyWrapper.iproxy.getStickyEventIds();

        for (String id : stickyEvents) {
            StickyEvent event = mStickyEventMap.get(id);
            if (event != null) {
                if (event.groupId == proxyWrapper.groupId) {
                    proxyWrapper.iproxy.post(id, event.objects);
                }
            }
        }
    }

    private ProxyWrapper initProxysMap(Object o, Object groupId, Iproxy iproxy) {
        if (iproxy != null) {
            iproxy.init(o);
            List<String> ids = iproxy.getEventIds();
            ProxyWrapper proxyWrapper = new ProxyWrapper(groupId, iproxy);
            //添加索引
            mIndexProxyMap.put(o, proxyWrapper);
            //id 对应处理的事件的类
            for (String id : ids) {
                List<ProxyWrapper> list = mProxysMap.get(id);
                if (list == null) {
                    list = new CopyOnWriteArrayList<>();
                    mProxysMap.put(id, list);
                }
                list.add(proxyWrapper);
            }
            return proxyWrapper;
        }

        return null;
    }

    public boolean register(Object o) {
        return register(o, null);
    }

    public synchronized void unregister(Object o) {
        if (o == null) {
            return;
        }
        ProxyWrapper proxyWrapper = mIndexProxyMap.remove(o);
        if (proxyWrapper != null) {
            for (String id : proxyWrapper.iproxy.getEventIds()) {
                List<ProxyWrapper> proxyWrappers = mProxysMap.get(id);
                proxyWrappers.remove(proxyWrapper);
            }
        }
    }

    //改造event 事件
    public void postTo(Object groupId, String id, Object... objects) {
        if (TextUtils.isEmpty(id)) {
            throw new InvalidParamException("id can not be null");
        }
        List<ProxyWrapper> proxyWrappers = mProxysMap.get(id);
        if (proxyWrappers == null) {
            //不存在的event id，不处理
            // throw new InvalidParamException("The id you posted may not be registered");
            return;
        }
        for (ProxyWrapper proxyWrapper : proxyWrappers) {
            if (proxyWrapper.groupId == groupId) {
                proxyWrapper.iproxy.post(id, objects);
            }
        }
    }

    public void post(String id, Object... objects) {
        postTo(null, id, objects);
    }

    public void postSticky(Object groupId, String id, Object... objects) {
        mStickyEventMap.put(id, new StickyEvent(groupId, objects));
        postTo(groupId, id, objects);
    }

    public void removeStickyEvent(String id) {
        mStickyEventMap.remove(id);
    }

    /**
     * Removes all sticky events.
     */
    public void removeAllStickyEvents() {
        mStickyEventMap.clear();
    }

    static class StickyEvent {
        public Object groupId;
        public Object[] objects;

        public StickyEvent(Object groupId, Object[] objects) {
            this.groupId = groupId;
            this.objects = objects;
        }
    }

    static class ProxyWrapper {
        public Object groupId;
        public Iproxy iproxy;

        public ProxyWrapper(Object groupId, Iproxy iproxy) {
            this.groupId = groupId;
            this.iproxy = iproxy;
        }
    }
}
