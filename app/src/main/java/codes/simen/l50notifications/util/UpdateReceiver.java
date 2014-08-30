package codes.simen.l50notifications.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UpdateReceiver extends BroadcastReceiver {

    public static final int LATEST_VERSION = 40;

    public UpdateReceiver() {
    }

    private static final String logTag = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Mlog.v(logTag, "receive");
        if (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            Mlog.v(logTag, "update");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

            final int prev_v = preferences.getInt("prev_v", LATEST_VERSION);
            if (prev_v < LATEST_VERSION) {
                SharedPreferences.Editor editor = preferences.edit();
                if (prev_v < 31) {
                    SharedPreferences oldPreferences = context.getSharedPreferences("heads-up", 0);
                    SharedPreferences.Editor oldEditor = oldPreferences.edit();
                    if (oldPreferences.contains("noshowlist")) {
                        Mlog.v(logTag, "noshowlist");
                        try {
                            oldPreferences.getString("noshowlist", null);
                        } catch (ClassCastException cce) {
                            Set<String> noshowlist = oldPreferences.getStringSet("noshowlist", new HashSet<String>());
                            try {
                                oldEditor.remove("noshowlist");
                                editor.putString("noshowlist", ObjectSerializer.serialize((Serializable) noshowlist));
                            } catch (IOException e) {
                                editor.putString("noshowlist", null);
                                e.printStackTrace();
                            }
                        }
                    }
                    if (oldPreferences.contains("blacklist")) {
                        Mlog.v(logTag, "blacklist");
                        try {
                            oldPreferences.getString("blacklist", null);
                        } catch (ClassCastException cce) {
                            Set<String> blacklist = oldPreferences.getStringSet("blacklist", new HashSet<String>());
                            try {
                                oldEditor.remove("blacklist");
                                editor.putString("blacklist", ObjectSerializer.serialize((Serializable) blacklist));
                            } catch (IOException e) {
                                editor.putString("blacklist", null);
                                e.printStackTrace();
                            }
                        }
                    }


                }
                editor.putInt("prev_v", LATEST_VERSION)
                      .apply();
            } else {
                Mlog.v(logTag, "already " + LATEST_VERSION);
            }
        }
    }
}
