package codes.simen.l50notifications;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

public class NotificationListenerAccessibilityService extends AccessibilityService
{
    static boolean isInit = false;
    private final static String logTag = "NotificationListenerAccessibility";

    public static boolean doLoadSettings = true;
    private VoiceOver voiceOver = null;
    private Set<String> packageBlacklist;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent)
	{
        if (accessibilityEvent.getEventType() ==
			AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
		{
            try {
                if (Build.VERSION.SDK_INT >= 18) return;

                // Ignore toasts
                Notification notification = (Notification) accessibilityEvent.getParcelableData();
                if (notification == null) return;
                // Ignore ongoing stuff
                if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) return;

                DecisionMaker decisionMaker = new DecisionMaker();
                decisionMaker.handleActionAdd(notification,
                        accessibilityEvent.getPackageName().toString(),
                        null,
                        0,
                        getApplicationContext(),
                        "accessibility");

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    String report = e.getMessage();
                    if (report == null) report = "";
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    report = report.concat( writer.toString() );
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (preferences != null && report.length() > 0) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("lastBug", report);
                        editor.apply();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onServiceConnected()
	{
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putBoolean("running", true)
                .apply();
        if (isInit)
		{
            return;
        }

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
        isInit = true;
        doLoadSettings();

        if (Build.VERSION.SDK_INT >= 18) {
            stopSelf();
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), OverlayServiceCommon.class);
            intent.setAction("STAY");
            intent.putExtra("packageName", getPackageName());
            intent.putExtra("title", getString(R.string.app_name));

            if (isNotificationListenerEnabled())
                intent.putExtra("text", getString(R.string.intro_warning_both_services));
            else {
                final String str = getString(R.string.accessibility_desc);
                intent.putExtra("text", str.substring(str.lastIndexOf("\n")+1));
            }
            intent.putExtra("action", PendingIntent.getActivity(getApplicationContext(), 0, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0));

            if (Build.VERSION.SDK_INT >= 11) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismiss_white);
                intent.putExtra("iconLarge", bitmap);
            }
            intent.putExtra("icon", R.drawable.ic_dismiss_white);
            startService(intent);
            stopSelf();
        }
    }

    private void doLoadSettings() {
        doLoadSettings = false;
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("music_on", false)) {
            if (voiceOver == null)
                voiceOver = new VoiceOver();
            voiceOver.enableVoiceOver(getApplicationContext());
        } else if (voiceOver != null)
            voiceOver.disableVoiceOver(getApplicationContext());
    }

    @Override
    public void onInterrupt()
	{
        isInit = false;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putBoolean("running", false)
                .apply();
    }

    private static boolean isInCall(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int mode = manager.getMode();
        if (mode == AudioManager.MODE_IN_CALL) {
            return true;
        } else if (Build.VERSION.SDK_INT >= 11) {
            if (mode == AudioManager.MODE_IN_COMMUNICATION) {
                return true;
            }
        }
        return false;
    }

    boolean isNotificationListenerEnabled() {
        Context context = getApplicationContext();
        try {
            //noinspection ConstantConditions
            ContentResolver contentResolver = context.getContentResolver();
            String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            String packageName = context.getPackageName();

            return !(enabledNotificationListeners == null
                    || !enabledNotificationListeners.contains(packageName));
        } catch (NullPointerException e) {
            return false;
        }
    }

}
