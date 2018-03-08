package com.enigma.message.slink;


import java.util.List;

/**
 * Created by shang on 17/5/4.
 */

public interface Iproxy {
    //将对象引用和代理类关联
    void init(Object object);

    //apt 生成 event ids 信息
    List<String> getEventIds();

    //sticky事件集合
    List<String> getStickyEventIds();

    // 根据id 和入参，执行对象的方法 
    void post(String id, Object[] objects);
}
