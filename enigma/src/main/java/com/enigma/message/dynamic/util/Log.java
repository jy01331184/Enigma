package com.enigma.message.dynamic.util;

/**
 * Created by tianyang on 16/8/3.
 */
public class Log {

    public static boolean Debug = true;
    public static final String TAG = "MessageCenter";

    public static void log(Object caller,String str)
    {
        if(Debug){
            android.util.Log.i(TAG,"["+caller+"] :"+str);
        }

    }

    public static void error(Object caller,String str)
    {
        if(Debug){
            android.util.Log.i(TAG,"["+caller+"] :"+str);
        }
    }
}
