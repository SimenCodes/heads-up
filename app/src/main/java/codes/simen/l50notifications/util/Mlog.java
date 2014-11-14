/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    public static void v (Object msg) {
        if (isLogging)
            Log.v("DUMP DATA", String.valueOf(msg));
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
