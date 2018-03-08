package com.enigma.object.util;


import com.enigma.object.PluginConstants;

/**
 * 日志工具类
 *
 * @author chuansi.wgl on 17/7/21
 */
public class PluginLogger {
    public static volatile PluginLogger mInstance;

    public static PluginLogger getInstance() {
        if (null == mInstance) {
            synchronized (PluginLogger.class) {
                if (null == mInstance) {
                    mInstance = new PluginLogger();
                }
            }
        }
        return mInstance;
    }

    public void i(String msg) {
        System.out.println(PluginConstants.PLUGIN_TAG + ": " + msg + getCodeLine());
    }

    public void i(String tag, String msg) {
        System.out.println(PluginConstants.PLUGIN_TAG + ": " + tag + ": " + msg + getCodeLine());
    }

    public void e(String msg) {
        System.out.println(PluginConstants.PLUGIN_TAG + ": Error: " + msg + getCodeLine());
    }

    public void e(String tag, String msg) {
        System.out.println(PluginConstants.PLUGIN_TAG + ": Error: " + tag + ": " + msg + getCodeLine());
    }

    private String getCodeLine() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace != null && trace.length > 3) {
            return "(" + trace[4].getFileName() + ":" + trace[4].getLineNumber() + ")";
        }
        return "";
    }
}
