package com.enigma.object.container.ognl;

import android.text.TextUtils;

import com.enigma.object.annotations.OGNL;
import com.enigma.object.util.Log;
import com.enigma.object.util.WrapUtil;

import java.util.List;

/**
 * {@link OgnlContext}的具体实现类。
 *
 * @author tianyang on 17/7/28.
 */
public class OgnlContextImpl implements OgnlContext {

    /**
     * 父上下文
     */
    private OgnlContext parent;
    /**
     * 当前上下文
     */
    private Object object;
    /**
     * Ognl的上下文中包含的字段(对象)集合
     */
    private List<OgnlField> fieldScanList;

    public OgnlContextImpl(Object object, List<OgnlField> fieldScanList) {
        this.object = object;
        this.fieldScanList = fieldScanList;
    }

    @Override
    public <T> T get(String ognl) {

        if (TextUtils.isEmpty(ognl)) {
            return null;
        }

        if (ognl.equals(OGNL.THIS)) {
            return (T) object;
        }

        if (parent != null) {
            T result = parent.get(ognl);
            if (result != null) {
                return result;
            }
        }

        if (fieldScanList != null) {
            for (OgnlField fieldScan : fieldScanList) {
                if (TextUtils.equals(fieldScan.ognl, ognl)) {
                    if (fieldScan.wrapped) {
                        Log.log("OCenterImpl_OgnlContextImpl", "OgnlContextImpl ognl " + ognl);
                        return makeObject(fieldScan);
                    } else {
                        Log.log("OCenterImpl_OgnlContextImpl", "OgnlContextImpl wrap " + ognl);
                        WrapUtil.wrap(object, this, fieldScan);
                        return makeObject(fieldScan);
                    }
                }
            }
        }

        Log.error("OCenterImpl_OgnlContextImpl", "OgnlContextImpl can't handle " + ognl + " with context:" + object);

        return null;
    }

    @Override
    public void setParent(OgnlContext parent) {
        this.parent = parent;
    }

    private <T> T makeObject(OgnlField fieldScan) {
        try {
            return (T) fieldScan.field.get(object);
        } catch (Exception e) {
            Log.error("OCenterImpl_OgnlContextImpl", e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "OgnlContextImpl{" +
                "object=" + object +
                "}";
    }
}
