package codes.simen.l50notifications.util;

import android.util.Log;

import codes.simen.l50notifications.BuildConfig;

/**
 * Log wrapper for easily enabling and disabling debugging.
 */
public class Mlog {
    public static final boolean isLogging = BuildConfig.DEBUG;

    public static void v (Object tag, Object msg) {
        if (isLogging)
            Log.v(String.valueOf(tag), String.valueOf(msg));
    }

    public static void d (Object tag, Object msg) {
        if (isLogging)
            Log.d(String.valueOf(tag), String.valueOf(msg));
    }

    public static void i (Object tag, Object msg) {
        if (isLogging)
            Log.i(String.valueOf(tag), String.valueOf(msg));
    }

    public static void w (String tag, String msg) {
        if (isLogging)
            Log.w(tag, msg);
    }

    public static void e (String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void wtf (String tag, String msg) {
        Log.wtf(tag, msg);
    }
}
