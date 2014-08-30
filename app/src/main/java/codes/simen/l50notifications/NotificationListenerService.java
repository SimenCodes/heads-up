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
            if (
                   (statusBarNotification.isOngoing() || !statusBarNotification.isClearable())
                && !statusBarNotification.getPackageName().equals("com.tencent.mobileqq") // QQ, popular Chinese social media
                )
                    return;

            if (NotificationListenerAccessibilityService.doLoadSettings) doLoadSettings();

            DecisionMaker decisionMaker = new DecisionMaker();
            decisionMaker.handleActionAdd(statusBarNotification.getNotification(),
                    statusBarNotification.getPackageName(),
                    statusBarNotification.getTag(),
                    statusBarNotification.getId(),
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
