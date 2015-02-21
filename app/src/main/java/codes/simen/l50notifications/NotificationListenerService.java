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

package codes.simen.l50notifications;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import codes.simen.l50notifications.ui.WelcomeActivity;
import codes.simen.l50notifications.util.Mlog;


@SuppressLint("NewApi")
public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    private final static String logTag = "NotificationListener";
    public static final String ACTION_CUSTOM = "codes.simen.l50notifications.NotificationListenerService.ACTION_CUSTOM";

    private VoiceOver voiceOver = null;
    private final IBinder mBinder = new LocalBinder();

    public NotificationListenerService() {
        Mlog.v(logTag, "Created listener");
    }

    public class LocalBinder extends Binder {
        NotificationListenerService getService() {
            return NotificationListenerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Mlog.d(logTag, "bind");
        if (intent.getAction().equals(ACTION_CUSTOM)) {
            super.onBind(intent);
            return mBinder;
        } else  {
            doLoadSettings();
            return super.onBind(intent);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        try {
            if(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                    getBoolean("show_non_cancelable", false)  ){
                /* if Show non-cancellable notifications is selected then need not check
                for Ongoing / Clearable as there will be ongoing notification by the background
                 service which is trying to display. Also it need not check for Chinese Social
                Media website as it will be displaying the notification from the same.
                if Show non-cancellable notifications is not selected then existing logic
                prevails
                 */
                if (
                        (statusBarNotification.isOngoing() || !statusBarNotification.isClearable())
                                && !statusBarNotification.getPackageName().equals("com.tencent.mobileqq") // QQ, popular Chinese social media
                        )
                    return;
            }

            if (NotificationListenerAccessibilityService.doLoadSettings) doLoadSettings();

            String statusBarNotificationKey = null;
            if (Build.VERSION.SDK_INT >= 20) statusBarNotificationKey = statusBarNotification.getKey();

            DecisionMaker decisionMaker = new DecisionMaker();

            decisionMaker.handleActionAdd(statusBarNotification.getNotification(),
                    statusBarNotification.getPackageName(),
                    statusBarNotification.getTag(),
                    statusBarNotification.getId(),
                    statusBarNotificationKey,
                    getApplicationContext(),
                    "listener");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Mlog.e(logTag, "NPE");
        }
    }

    private void doLoadSettings() {
        NotificationListenerAccessibilityService.doLoadSettings = false;
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("music_on", false)) {
            if (voiceOver == null)
            voiceOver = new VoiceOver();
            voiceOver.enableVoiceOver(getApplicationContext());
        } else if (voiceOver != null)
            voiceOver.disableVoiceOver(getApplicationContext());

        if (isAccessibilityEnabled()) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), OverlayServiceCommon.class);
            intent.setAction("TEST");
            intent.putExtra("packageName", getPackageName());
            intent.putExtra("title", getString(R.string.app_name));
            intent.putExtra("text", getString(R.string.intro_warning_both_services));
            intent.putExtra("action", PendingIntent.getActivity(getApplicationContext(), 0, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0));

            if (Build.VERSION.SDK_INT >= 11) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismiss_white);
                intent.putExtra("iconLarge", bitmap);
            }
            intent.putExtra("icon", R.drawable.ic_dismiss_white);
            startService(intent);

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        DecisionMaker decisionMaker = new DecisionMaker();
        decisionMaker.handleActionRemove(
                statusBarNotification.getPackageName(),
                statusBarNotification.getTag(),
                statusBarNotification.getId(),
                getApplicationContext()
        );
    }

    public void doRemove (String pkg, String tag, int id) {
        Mlog.d(logTag, pkg + tag + id);
        try {
            cancelNotification(pkg, tag, id);
        } catch (SecurityException e) {
            try {
                String report = e.getMessage();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                report = report.concat( writer.toString() );
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("lastBug", report);
                editor.apply();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void doRemove (String key) {
        Mlog.d(logTag, key);
        try {
            cancelNotification(key);
        } catch (SecurityException e) {
            try {
                String report = e.getMessage();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                report = report.concat( writer.toString() );
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("lastBug", report);
                editor.apply();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public boolean isNotificationValid(String pkg, String tag, int id) {
        final StatusBarNotification[] activeNotifications = getActiveNotifications();
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            final String statusBarNotificationTag = statusBarNotification.getTag();
            final String statusBarNotificationPackageName = statusBarNotification.getPackageName();
            final int statusBarNotificationId = statusBarNotification.getId();
            if (statusBarNotificationPackageName.equals(pkg)
                    && statusBarNotificationId == id) {
                if (tag == null && statusBarNotificationTag == null)
                    return true;
                if (tag != null && statusBarNotificationTag != null)
                    if (statusBarNotificationTag.equals(tag))
                        return true;
            }
        }
        return false;
    }

    /*
    TODO: Doesn't work, see VoiceOver.java
    public void pushMusicNotification (String pkg) {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();
        if (statusBarNotifications.length > 0) {
            for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                final String statusBarNotificationPackageName = statusBarNotification.getPackageName();
                Mlog.v(pkg, statusBarNotificationPackageName);
                //if (pkg.contains(statusBarNotificationPackageName) && !statusBarNotificationPackageName.equals("android")) {
                if (statusBarNotificationPackageName.equals("com.google.android.music")) {
                    DecisionMaker decisionMaker = new DecisionMaker();
                    decisionMaker.handleActionAdd(statusBarNotification.getNotification(),
                            statusBarNotificationPackageName,
                            statusBarNotification.getTag(),
                            statusBarNotification.getId(),
                            getApplicationContext(),
                            "music");
                }
            }
        }
    }*/

    boolean isAccessibilityEnabled(){
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Mlog.w(logTag, "Error finding accessibility setting: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1){
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Mlog.d(logTag, "Setting: " + accessibilityService);
                    if (accessibilityService.equalsIgnoreCase(WelcomeActivity.ACCESSIBILITY_SERVICE_NAME)){
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
