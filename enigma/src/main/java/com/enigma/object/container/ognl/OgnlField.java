package com.enigma.object.container.ognl;


import com.enigma.object.model.scan.FieldScan;

/**
 * todo: 添加注释
 *
 * @author tianyang on 17/7/28.
 */
public class OgnlField extends FieldScan {

    public boolean wrapped;

    public OgnlField(FieldScan fieldScan) {
        this.field = fieldScan.field;
        this.cls = fieldScan.cls;
        this.id = fieldScan.id;
        this.constructorInfo = fieldScan.constructorInfo;
        this.singleton = fieldScan.singleton;
        this.async = fieldScan.async;
        this.ognl = fieldScan.ognl;
        this.hasImpl = fieldScan.hasImpl;
    }
}
