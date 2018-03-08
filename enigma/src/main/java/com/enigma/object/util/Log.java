package com.enigma.object.util;

/**
 * Created by tianyang on 16/8/3.
 */
public class Log {

    public static boolean Debug = true;
    public static final String TAG = "ObjectCenter";

    public static void log(Object caller, String str) {
        if (Debug) {
            android.util.Log.i(TAG, "{" + Thread.currentThread().getName() + "}" + "[" + caller + "] :" + str);
        }

    }

    public static void error(Object caller, String str) {
        if (Debug) {
            android.util.Log.i(TAG, "{" + Thread.currentThread().getName() + "}" + "[" + caller + "] :" + str);
        }
    }

    public static void error(Object caller, Throwable throwable) {
        if (Debug) {
            android.util.Log.e(TAG, "[" + caller + "] :", throwable);
        }
    }
}
