package com.enigma.object.container;

import android.text.TextUtils;

import com.enigma.object.base.AsyncAware;
import com.enigma.object.base.AsyncFinishTask;
import com.enigma.object.container.ognl.OgnlContext;
import com.enigma.object.container.ognl.OgnlContextImpl;
import com.enigma.object.container.ognl.OgnlField;
import com.enigma.object.model.info.ConstructorInfo;
import com.enigma.object.model.info.CreateInfo;
import com.enigma.object.model.scan.ClassScan;
import com.enigma.object.model.scan.ConstructorParamScan;
import com.enigma.object.model.scan.ConstructorScan;
import com.enigma.object.model.scan.FieldScan;
import com.enigma.object.util.Log;
import com.enigma.object.util.ScanUtil;
import com.enigma.object.util.WrapUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyang on 17/7/14.
 */
public class OCenterImpl extends OCenter {

    private boolean autoWrapEnable = false;
    private Map<Class, Object> SINGLETON = new ConcurrentHashMap<>();
    private Map<String, Object> OBJ = new ConcurrentHashMap<>();

    private Map<Class,Class> CLASS_BIND = new HashMap<>();

    private Map<Class, ClassScan> CLASS_SCANS = new HashMap<>();
    private Map<Class, List<FieldScan>> FIELD_SCANS = new HashMap<>();
    private Map<String, ConstructorScan> CONSTRUCTOR_SCANS = new HashMap<>();

    private Map<String, Class> ASYNC_PROXY_CLASS = new HashMap<>();
    private Set<String> ASYNC_PROXY_SKIP_CLASS = new HashSet<>();

    private ExecutorService service = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 30, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    @Override
    public synchronized <T> T getOrCreateObjectById(String id, Class<T> cls, ConstructorInfo constructorInfo) {
        return getOrCreateObjectImpl(new CreateInfo(cls, id, false, false), constructorInfo);
    }

    @Override
    public synchronized <T> T getOrCreateObjectById(CreateInfo createInfo, ConstructorInfo constructorInfo) {
        return getOrCreateObjectImpl(createInfo, constructorInfo);
    }

    @Override
    public <T> T getObjectById(String id) {
        if (TextUtils.isEmpty(id))
            return null;
        return (T) OBJ.get(id);
    }

    private void removeObjectFields(Object object) {
        if(object == null){
            return ;
        }

        List<FieldScan> fieldScanList = FIELD_SCANS.get(object.getClass());
        if (fieldScanList == null) {
            fieldScanList = ScanUtil.scanField(object.getClass());
            FIELD_SCANS.put(object.getClass(), fieldScanList);
        }
        for (FieldScan fieldScan : fieldScanList) {
            if( !TextUtils.isEmpty(fieldScan.id) ){
                removeObjectById(fieldScan.id,true);
            }
        }
    }

    @Override
    public void removeObjectById(String id,boolean recursive) {
        Object obj = OBJ.remove(id);
        if(recursive){
            removeObjectFields(obj);
        }
    }

    @Override
    public void bind(Class cls, Class bindCls) {
        CLASS_BIND.put(cls,bindCls);
    }

    @Override
    public void dump() {
        Set<String> keys = OBJ.keySet();
        for (String key : keys) {
            System.out.println(key + ":---:" + OBJ.get(key));
        }
    }

    @Override
    public void enableAutoWrap() {
        autoWrapEnable = true;
    }

    /**
     * ============================================================ 分割线 ============================================================
     */

    private <T> T getOrCreateObjectImpl(CreateInfo createInfo, ConstructorInfo constructorInfo) {
        String id = createInfo.id;
        T object;

        if ((object = (T) SINGLETON.get(createInfo.cls)) != null) {
            Log.log("APContainerImpl", "use SINGLETON:" + createInfo.cls.getName() );
            return object;
        } else if (!TextUtils.isEmpty(id) && (object = getObjectById(id)) != null) {
            Log.log("APContainerImpl", "use id:" + id );
            return object;
        }

        object = accquire(constructorInfo, createInfo);
        //Log.log("APContainerImpl", "return id:" + id + "," + "cls:" + createInfo.cls.getName());
        return object;
    }

    private <T> T accquire(ConstructorInfo constructorInfo, CreateInfo createInfo) {

        if(!createInfo.hasImpl){
            Class bindClass = CLASS_BIND.get(createInfo.cls);
            if(bindClass != null)
                createInfo.cls = bindClass;
        }


        ClassScan classScan = CLASS_SCANS.get(createInfo.cls);
        if (classScan == null) {
            classScan = ScanUtil.scanClass(createInfo.cls);
            CLASS_SCANS.put(createInfo.cls, classScan);
        }

        try {
            T object = null;

            if (createInfo.async) {
                object = generateAsyncProxy(createInfo);
                if(object == null){
                    service.execute(new AsyncTask(createInfo,constructorInfo));
                }
            } else {
                ConstructorScan constructorScan = getConstructorScan(createInfo, constructorInfo);
                //Log.log("APContainerImpl", "start accquire:" + constructorScan);

                if (constructorScan != null) {
                    List<ConstructorParamScan> parameters = constructorScan.parameters;
                    if(parameters.isEmpty()){
                        object = (T) constructorScan.constructor.newInstance();
                    } else {
                        Object[] args = new Object[constructorScan.parameters.size()];

                        for (int i = 0; i < args.length; i++) {
                            ConstructorParamScan paramScan = parameters.get(i);
                            if(TextUtils.isEmpty(paramScan.ognl)){
                                CreateInfo tempCreateInfo = new CreateInfo(paramScan.cls, paramScan.id, paramScan.singleton, false);
                                tempCreateInfo.hasImpl = paramScan.hasImpl;
                                tempCreateInfo.ognlContext = createInfo.ognlContext;
                                args[i] = getOrCreateObjectImpl(tempCreateInfo, paramScan.constructorInfo);
                            } else {
                                if(createInfo.ognlContext == null){
                                    Log.log("APContainerImpl", "ConstructorScan ognl :" + paramScan.ognl + " with null ognl context,"+paramScan+"--->"+constructorScan);
                                } else {
                                    args[i] = createInfo.ognlContext.get(paramScan.ognl);
                                }
                            }
                            Log.log("APContainerImpl", "ConstructorScan Param id:" + paramScan.id + " cls:" + paramScan.cls + " ->" + args[i]);
                        }
                        if(constructorScan.constructor == null ){
                            object = (T) createInfo.cls.newInstance();
                        } else {
                            object = (T) constructorScan.constructor.newInstance(args);
                        }
//                        Log.log("APContainerImpl", "finish accquire :" + object );
                    }
                } else {
                    Log.error("APContainerImpl", "null ConstructorScan with id:" + constructorInfo.constructorId + " cls:" + createInfo.cls.getName());
                    return null;
                }
            }

            if ( (createInfo.singleton || classScan.singleton) && object != null) {
                SINGLETON.put(createInfo.cls, object);
            }
            if (!TextUtils.isEmpty(createInfo.id) && object != null) {
                OBJ.put(createInfo.id, object);
            }

            if ( (autoWrapEnable && classScan.autoWrap) || createInfo.async ) {

            } else {
                wrapField(object,createInfo.ognlContext);
            }

            return object;
        } catch (Exception e) {
            Log.error("APContainerImpl", e);
        }

        return null;
    }

    public void wrapField(Object object) {
        if(object == null){
            return ;
        }
        wrapField(object,null);
        //Log.log("wrapField","wrapField "+object.getClass()+" use:"+(System.currentTimeMillis()-time)+"ms");
    }

    private void wrapField(Object object,OgnlContext parent) {
        if(object == null){
            return ;
        }

        List<FieldScan> fieldScanList = FIELD_SCANS.get(object.getClass());
        if (fieldScanList == null) {
            fieldScanList = ScanUtil.scanField(object.getClass());
            FIELD_SCANS.put(object.getClass(), fieldScanList);
        }

        if(fieldScanList.isEmpty())
            return ;
        //Log.log("APContainerImpl", "base wrap field :" + object +":->"+parent);

        List<OgnlField> ognlFields = new LinkedList<>();
        for (FieldScan fieldScan : fieldScanList) {
            ognlFields.add(new OgnlField(fieldScan));
        }

        OgnlContext ognlContext = new OgnlContextImpl(object,ognlFields);
        ognlContext.setParent(parent);

        for (OgnlField fieldScan : ognlFields) {
            WrapUtil.wrap(object,ognlContext,fieldScan);
        }
    }

    private ConstructorScan getConstructorScan(CreateInfo createInfo, ConstructorInfo constructorInfo) {

        String key = createInfo.cls.getName() + "_" + (constructorInfo == null?"_null_":constructorInfo.constructorId);
        ConstructorScan constructorScan = CONSTRUCTOR_SCANS.get(key);

        if (constructorScan == null) {
            constructorScan = ScanUtil.scanConstructor(createInfo.cls, constructorInfo);
            CONSTRUCTOR_SCANS.put(key, constructorScan);
        }

        return constructorScan;
    }

    private <T> T generateAsyncProxy(CreateInfo createInfo) {
        String asyncClassName = createInfo.cls.getName() + "_ASYNC";
        if(ASYNC_PROXY_SKIP_CLASS.contains(asyncClassName)){
            return null;
        }

        try {
            Class asyncClass = ASYNC_PROXY_CLASS.get(asyncClassName);
            if (asyncClass == null) {
                asyncClass = Class.forName(asyncClassName);
                ASYNC_PROXY_CLASS.put(asyncClassName, asyncClass);
            }
            Constructor constructor = asyncClass.getDeclaredConstructor(new Class[]{OCenter.class, ExecutorService.class, CreateInfo.class, ConstructorInfo.class,AsyncFinishTask.class});
            CreateInfo tempCreateInfo = new CreateInfo(createInfo.cls, null, false, false);

            AsyncFinishTask task = createInfo.host instanceof AsyncAware ? new AsyncFinishTask((AsyncAware) createInfo.host,createInfo.id) : null;

            T object = (T) constructor.newInstance(this, service, tempCreateInfo, null, task);
            return object;
        }catch (Exception e){
            Log.error("APContainerImpl_ASYNC_DETAIL", e.toString());
            ASYNC_PROXY_SKIP_CLASS.add(asyncClassName);
        }
        return null;
    }


    class AsyncTask implements Runnable{

        private CreateInfo createInfo;
        private ConstructorInfo constructorInfo;

        public AsyncTask(CreateInfo createInfo, ConstructorInfo constructorInfo) {
            this.createInfo = createInfo;
            this.constructorInfo = constructorInfo;
        }

        @Override
        public void run() {
            Log.log("APContainerImpl","async before:"+createInfo.host+"->"+createInfo.fieldName);
            CreateInfo invokeCreateInfo = new CreateInfo(createInfo.cls,createInfo.id,createInfo.singleton,false);
            Object arg = getOrCreateObjectImpl(invokeCreateInfo,constructorInfo);
            Log.log("APContainerImpl","async after:"+createInfo.host+"->"+createInfo.fieldName);

            try {
                Field field = createInfo.field;
                if(field == null) {
                    field = createInfo.host.getClass().getDeclaredField(createInfo.fieldName);
                }
                field.setAccessible(true);
                field.set(createInfo.host,arg);

                if(createInfo.host instanceof AsyncAware){
                    AsyncAware asyncAware = (AsyncAware) createInfo.host;
                    asyncAware.onLoad(null,arg,createInfo.id);
                }

            } catch (Exception e) {
                Log.error("APContainerImpl",e);
            }
        }
    }
}
