package com.enigma.object.coderesolve;

import com.enigma.object.util.Log;
import com.squareup.javapoet.MethodSpec;

/**
 * 辅助用于自动生成的性能监控代码
 *
 * @author chuansi.wgl on 8/2/17
 */
public class PerformanceMonitor {
    private static final String PERF_LOG_TAG = "Performance Monitor";

    public static void insertTimeStartPoint(MethodSpec.Builder methodBuilder, String tag) {
        methodBuilder.addStatement("long $L = $T.currentTimeMillis()", tag, System.class);
    }

    public static void insertTimeEndPointWithLog(MethodSpec.Builder methodBuilder, String tag) {
        methodBuilder.addStatement("long timeElapse = $T.currentTimeMillis() - $L", System.class, tag);
        methodBuilder.addStatement("$T.log($S,$T.valueOf(timeElapse))", Log.class, PERF_LOG_TAG, String.class);
    }
}
