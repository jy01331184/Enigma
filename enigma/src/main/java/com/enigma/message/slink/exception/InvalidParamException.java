package com.enigma.message.slink.exception;

/**
 * Created by shang[王岩] on 17/6/8
 * fanshang.wy@antfin.com
 */

public class InvalidParamException extends RuntimeException {
    public InvalidParamException(String s){
        super("InvalidParamException:"+ s);
    }
}
