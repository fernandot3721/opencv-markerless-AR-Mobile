package org.opencv.samples.tutorial2;

import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by tangjp on 16-12-6.
 */

public class PerformanceAnalyzer {
    private static String LogTAG = "PerformanceAnalyzer";
    private static long sBegin;

    public static void log() {
        sBegin = System.currentTimeMillis();
    }

    public static long count(String msg) {
        long now = System.currentTimeMillis();
        long diff = now - sBegin;
        Log.e(LogTAG, "[" + msg + "] : " + diff);
//        Log.e(LogTAG, "[" + msg + "] | seconds: " + TimeUnit.MILLISECONDS.toSeconds(diff) +
//                " | minutes: " + TimeUnit.MILLISECONDS.toMinutes(diff));
        return now;
    }

    public static void logAndCount(String msg) {
        sBegin = count(msg);
    }
}
