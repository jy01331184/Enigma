package com.enigma.object.util;


import com.enigma.object.container.OCenter;
import com.enigma.object.container.ognl.OgnlContext;
import com.enigma.object.container.ognl.OgnlField;
import com.enigma.object.model.info.CreateInfo;

/**
 * 对一个类属性的填充工具类
 *
 * @author tianyang on 17/7/14.
 */
public class WrapUtil {

    public static void wrap(Object object, OgnlContext ognlContext, OgnlField fieldScan) {

        if (fieldScan.wrapped) {
            return;
        }

        CreateInfo createInfo = new CreateInfo(fieldScan.cls, fieldScan.id, fieldScan.singleton, fieldScan.async);
        createInfo.ognlContext = ognlContext;
        createInfo.ognl = fieldScan.ognl;
        if (createInfo.async) {
            createInfo.host = object;
            createInfo.field = fieldScan.field;
        }

        Object arg = OCenter.getInstance().getOrCreateObjectById(createInfo, fieldScan.constructorInfo);

        try {
            fieldScan.field.set(object, arg);
            fieldScan.wrapped = true;
        } catch (IllegalAccessException e) {
            Log.error("OCenterImpl", e);
        }

    }


}
