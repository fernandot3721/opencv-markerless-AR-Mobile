package meiroo.cvgl;

import android.annotation.SuppressLint;
import android.util.Log;


public class PerformanceAnalyzer {
    private static String LogTAG = "PerformanceAnalyzerJ";
    private static long sBegin;

    public static void log() {
        sBegin = System.currentTimeMillis();
    }

    @SuppressLint("LongLogTag")
    public static long count(String msg) {
        long now = System.currentTimeMillis();
        long diff = now - sBegin;
        Log.e(LogTAG, "[" + msg + "] : " + diff);
//        Log.e(LogTAG, "["  msg  "] | seconds: "  TimeUnit.MILLISECONDS.toSeconds(diff)
//                " | minutes: "  TimeUnit.MILLISECONDS.toMinutes(diff));
        return now;
    }

    public static void logAndCount(String msg) {
        sBegin = count(msg);
    }
}
