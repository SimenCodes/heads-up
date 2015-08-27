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

package codes.simen.l50notifications.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import codes.simen.l50notifications.OverlayServiceCommon;
import codes.simen.l50notifications.R;
import codes.simen.l50notifications.util.Mlog;

public class WelcomeActivity extends Activity {
    public static final String ACCESSIBILITY_SERVICE_NAME = "codes.simen.l50notifications/codes.simen.l50notifications.NotificationListenerAccessibilityService";
    private static final String logTag = "Heads-up";
    private static boolean isRunning = false;
    private SharedPreferences preferences = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);
        setContentView(R.layout.activity_welcome);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (preferences.getBoolean("firstrun", true)) {
            startActivity(new Intent(this, SetupActivity.class));
        }

        if (Build.DISPLAY.toUpperCase().contains("MIUI") || Build.MANUFACTURER.toUpperCase().contains("XIAOMI")) {
            findViewById(R.id.miui_warning).setVisibility(View.VISIBLE);
        }
        if (Mlog.isLogging) {
            doSendTest(null);
            Toast.makeText(getApplicationContext(),
                    "Experimental demo version. Does not auto-update, and might not work at all. Please report bugs!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        isRunning = true;
        //TextView status = (TextView) findViewById(R.id.status);
        Button enableButton = (Button) findViewById(R.id.notification_open);
        if (
                ( Build.VERSION.SDK_INT >= 18 && isNotificationListenerEnabled(this) )
                || isAccessibilityEnabled(getApplicationContext())
        ) {
            //status.setVisibility(View.VISIBLE);
            enableButton.setBackgroundResource(R.drawable.button_enable_on);
            checkEnabled();
            if (( Build.VERSION.SDK_INT >= 18 && isNotificationListenerEnabled(this) )
                    && isAccessibilityEnabled(getApplicationContext()) ) {
                final View bothEnabled = findViewById(R.id.bothEnabled);
                bothEnabled.setVisibility(View.VISIBLE);
                bothEnabled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoAccessibility(v.getContext());
                    }
                });
            } else {
                findViewById(R.id.bothEnabled).setVisibility(View.GONE);
            }
        } else {
            enableButton.setBackgroundResource(R.drawable.button_enable);
            //status.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        isRunning = false;
    }

    private void checkEnabled() {
        if (preferences.getBoolean("running", false)) {
            TextView status = (TextView) findViewById(R.id.status);
            status.setText(getString(R.string.intro_status_on_confirmed));
        } else if (isRunning) {
            //Mlog.d(logTag, "handler");
            handler.postDelayed(runnable, 5000);
        }
    }
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkEnabled();
        }
    };

    public void onClick (View v) {
        preferences.edit()
                .putBoolean("running", false)
                .apply();
        if (Build.VERSION.SDK_INT >= 18)
            gotoNotifyservice(this);
        else
            gotoAccessibility(this);
    }

    public void doOpenSettings (View v) {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    public void doSendTest (View v) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), OverlayServiceCommon.class);
        intent.setAction("TEST");
        intent.putExtra("packageName", getPackageName());
        intent.putExtra("title", "Simen.codes");
        intent.putExtra("text", "Thanks for trying Heads-up! If you like it, please leave a review on Play. If you can\'t get it to work, you can get help on the project's GitHub issue page.");
        intent.putExtra("action", PendingIntent.getActivity(this, 0,
                new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("http://simen.codes"))
                , PendingIntent.FLAG_UPDATE_CURRENT));

        if (Build.VERSION.SDK_INT >= 11) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            intent.putExtra("iconLarge", bitmap);
        }/**/
        intent.putExtra("icon", -1);
        intent.putExtra("color", getResources().getColor(R.color.primaryDark, null));

        intent.putExtra("actionCount", 2);
        intent.putExtra("action2title", getString(R.string.action_settings));
        intent.putExtra("action2icon", R.drawable.ic_action_settings);
        intent.putExtra("action2intent", PendingIntent.getActivity(this, 0,
                new Intent(getApplicationContext(), SettingsActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT));
        intent.putExtra("action1title", getString(R.string.action_donate));
        intent.putExtra("action1icon", R.drawable.ic_coin);
        intent.putExtra("action1intent", PendingIntent.getActivity(this, 0,
                new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("http://simen.codes/donate/#heads-up"))
                , PendingIntent.FLAG_UPDATE_CURRENT));

        startService(intent);

        Mlog.v(logTag, "open");
    }

    public void getHelp (View v) {
        startActivity(new Intent(
                Intent.ACTION_VIEW, Uri.parse("https://github.com/SimenCodes/heads-up/blob/master/README.md#common-issues")
        ));
    }

    public void doReport (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        try {
            final int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            intent.putExtra(Intent.EXTRA_TEXT,
                    preferences
                            .getString("lastBug", "Bug not saved") +
                            "--" + preferences.getBoolean("running", false) + " - " +
                            Build.VERSION.SDK_INT + " - " + versionCode + " - " + Build.PRODUCT
            );
            intent.putExtra(Intent.EXTRA_TITLE, "Bug in Heads-up " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            intent.putExtra(Intent.EXTRA_TEXT,
                    preferences
                            .getString("lastBug", "Bug not saved") +
                            "--" + preferences.getBoolean("running", false) + " - " +
                            Build.VERSION.SDK_INT + " - unknown version" + " - " + Build.PRODUCT
            );
            intent.putExtra(Intent.EXTRA_TITLE, "Bug in Heads-up");
        }
        startActivity(Intent.createChooser(intent, "Select export location"));

        preferences
                .edit()
                .remove("lastBug")
                .apply();
    }

    public void doOpenSite(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://simen.codes/"));
        startActivity(intent);
    }


    public static void gotoNotifyservice(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                context.startActivity(intent);
                Toast.makeText(context, context.getText(R.string.notification_listener_not_found_detour), Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException anfe2) {
                Toast.makeText(context, anfe2.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void gotoAccessibility(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
            Toast.makeText(context, context.getText(R.string.accessibility_toast), Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
                Toast.makeText(context, context.getText(R.string.accessibility_not_found_detour), Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException anfe2) {
                Toast.makeText(context, anfe2.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    public static boolean isAccessibilityEnabled(Context context){
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Mlog.d(logTag, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Mlog.d(logTag, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1){
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Mlog.d(logTag, "Setting: " + settingValue);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Mlog.d(logTag, "Setting: " + accessibilityService);

                    if (accessibilityService.equalsIgnoreCase(ACCESSIBILITY_SERVICE_NAME)){
                        Mlog.d(logTag, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    } else if (Build.VERSION.SDK_INT < 18 && "com.pushbullet.android/com.pushbullet.android.notifications.mirroring.CompatNotificationMirroringService".equals(accessibilityService)) {
                        // For easier translation in case of other troublesome services
                        Toast.makeText(context, String.format(context.getString(R.string.accessibility_service_blocked),
                                "PushBullet Notification Mirroring"), Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
        else{
            Mlog.d(logTag, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    public static boolean isNotificationListenerEnabled(Context context) {
        try {
            //noinspection ConstantConditions
            ContentResolver contentResolver = context.getContentResolver();
            String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            String packageName = context.getPackageName();

            // check to see if the enabledNotificationListeners String contains our package name
            return !(enabledNotificationListeners == null
                    || !enabledNotificationListeners.contains(packageName));
        } catch (NullPointerException e) {
            return false;
        }
    }
}
