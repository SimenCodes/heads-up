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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import codes.simen.l50notifications.admin.AdminReceiver;
import codes.simen.l50notifications.theme.HoloDark;
import codes.simen.l50notifications.theme.HoloLight;
import codes.simen.l50notifications.theme.L5Black;
import codes.simen.l50notifications.theme.L5Dark;
import codes.simen.l50notifications.theme.L5Light;
import codes.simen.l50notifications.theme.Random;
import codes.simen.l50notifications.theme.ThemeClass;
import codes.simen.l50notifications.theme.Ubuntu;
import codes.simen.l50notifications.util.Mlog;
import codes.simen.l50notifications.util.ObjectSerializer;
import codes.simen.l50notifications.util.SwipeDismissTouchListener;

public class OverlayServiceCommon extends Service implements SensorEventListener {
    final static String logTag = "Overlay";
    public static final int MAX_DISPLAY_TIME = 60000;
    public static final int MAX_REMINDER_TIME = 1200000;
    public static final int MIN_REMINDER_TIME = 6000;
    private static final int MAX_LINES = 12;
    private static final int SENSOR_DELAY_MILLIS = 10000;
    private static final int MIN_LINES = 2;
    public static final int FLAG_FLOATING_WINDOW = 0x00002000;
    private static final ArrayList<String> LOCKSCREEN_APPS = new ArrayList<String>(Arrays.asList(new String[]{
            "com.achep.acdisplay",
            "com.silverfinger.lockscreen",
            "com.slidelock",
            "com.coverscreen.cover",
            "com.jiubang.goscreenlock",
            "com.greatbytes.activenotifications",
            "com.nemis.memlock",
            "com.teslacoilsw.widgetlocker",
            "com.jedga.peek",
            "com.jedga.peek.free",
            "com.jedga.peek.pro",
            "com.hi.locker",
            "com.vlocker.locker"
    }));

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private LinearLayout layout;
    private boolean isViewAdded = false;
    private ThemeClass themeClass = new ThemeClass();

    private SharedPreferences preferences = null;

    private PendingIntent pendingIntent;
    private int displayTime = 15000;
    private int position = 1;
    private String currentPackage = "";
    private boolean isCompact = false;
    private boolean isActionButtons = false;

    private SensorManager sensorManager = null;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private DevicePolicyManager policyManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wLock;
    private boolean isProximityClose = true;
    private boolean isLocked;

    String packageName = "";
    String tag = "";
    String key = "";
    private String prevPackageName = "0";

    int id = 0;

    @Override
    public void onCreate () {
        super.onCreate();
        try {
            Mlog.d(logTag, "Create");
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            displayTime = preferences.getInt("overlay_display_time", 8000);
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            LayoutInflater inflater = LayoutInflater.from(this);

            getCurrentPackage();
            isLocked = isLocked();

            layout = new LinearLayout(this);
            inflater.inflate(R.layout.activity_read, layout);
            layout.setVisibility(View.GONE);
            ViewStub stub = (ViewStub) layout.findViewById(R.id.viewStub);

            if (!getResources().getBoolean(R.bool.is_tablet)) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                final ViewGroup.LayoutParams stubLayoutParams = stub.getLayoutParams();
                if (metrics.widthPixels <= metrics.heightPixels)
                    stubLayoutParams.width = metrics.widthPixels;
                else
                    //noinspection SuspiciousNameCombination
                    stubLayoutParams.width = metrics.heightPixels;
                stub.setLayoutParams(stubLayoutParams);
            }

            final int theme = Integer.parseInt(preferences.getString("overlay_style", "0"));
            switch (theme) {
                case 0: // L
                    themeClass = new L5Light(stub);
                    break;
                case 1: // L Dark
                    themeClass = new L5Dark(stub);
                    break;
                case 5: // L Black
                    themeClass = new L5Black(stub);
                    break;
                case 2: // Holo Light
                    themeClass = new HoloLight(stub);
                    break;
                case 3: // Holo
                    themeClass = new HoloDark(stub);
                    break;
                case 4: // Random
                    themeClass = new Random(stub);
                    break;
                case 6: // Ubuntu
                    themeClass = new Ubuntu(stub);
                    break;
            }
            stub.inflate();
            themeClass.init(layout);

            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
            );
            if (isLocked)
                position = Integer.valueOf(preferences.getString("overlay_vertical_position_locked", "-10"));
            if (!isLocked || position == -10)
                position = Integer.valueOf(preferences.getString("overlay_vertical_position", "1"));
            switch (position) {
                case 2:
                    layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                    layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    break;
                case 1:
                    layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    break;
                case 0:
                    layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                    break;
                case -1:
                    layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    break;
            }

            isCompact = preferences.getBoolean("compact_mode", true);

        } catch (VerifyError ve) {
            Mlog.w(logTag, ve.getMessage());
        }
    }

    private void getCurrentPackage() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            currentPackage = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        } catch (SecurityException | IndexOutOfBoundsException e) {
            reportError(e, "Please allow Heads-up to get running tasks", getApplicationContext());
        }
    }

    private void displayWindow () {
        if (preferences.getBoolean("lock_screen_on", false)) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            if (preferences.getBoolean("only_on_lock_screen", false)) {
                if (stopIfNotLocked()) return;

            }
            if (powerManager == null)
                powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            if (preferences.getBoolean("turn_screen_on", false) && !powerManager.isScreenOn()) {
                if (preferences.getBoolean("use_proximity", false)) {
                    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    if (sensor == null) {
                        try {
                            sensor = sensorManager.getSensorList(Sensor.TYPE_PROXIMITY).get(0);
                        } catch (Exception sensorListException) {
                            if (Mlog.isLogging) sensorListException.printStackTrace();
                        }
                    }
                    if (sensor != null) {
                        addViewToWindowManager();
                        sensorEventListener = this;
                        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
                        handler.postDelayed(sensorChecker, SENSOR_DELAY_MILLIS);
                    } else {
                        reportError(null, getString(R.string.no_proximity_sensor_error), getApplicationContext());
                        preferences.edit().putBoolean("use_proximity", false).apply();
                        createWLock();
                        screenOn();
                        addViewToWindowManager();
                    }
                } else {
                    createWLock();
                    screenOn();
                    addViewToWindowManager();
                }
            } else {
                addViewToWindowManager();
            }
        } else {
            if (isLocked()) stopSelf();
            else
                addViewToWindowManager();
        }
    }

    private boolean stopIfNotLocked() {
        if (isLocked) return false;
        Mlog.d(logTag, "not locked");
        stopSelf();
        return true;
    }

    private boolean isLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        final boolean isLocked;
        if (Build.VERSION.SDK_INT >= 16)
             isLocked = keyguardManager.isKeyguardLocked();
        else isLocked = keyguardManager.inKeyguardRestrictedInputMode();

        Mlog.v(logTag, isLocked + " " + LOCKSCREEN_APPS.contains(currentPackage));
        return isLocked || (currentPackage != null && LOCKSCREEN_APPS.contains(currentPackage));
    }

    private void addViewToWindowManager() {
        if (!isViewAdded) {
            windowManager.addView(layout, layoutParams);
			layout.requestFocus();
	    }
        isViewAdded = true;
    }

    private final Runnable sensorChecker = new Runnable() {
        @Override
        public void run() {
            Mlog.d(logTag + "SensorChecker", String.valueOf(isProximityClose));

            if (sensorManager != null) {
                sensorManager.unregisterListener(sensorEventListener, sensor);
                sensorManager = null;
            }
        }
    };

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Mlog.d(logTag, "Start");
        try {
            if (intent.getAction().equals("REMOVE")) {
                try {
                    if (packageName.equals(intent.getStringExtra("packageName"))
                            && tag.equals(intent.getStringExtra("tag"))
                            && id == intent.getIntExtra("id", 0)) {
                        Mlog.d(logTag, "remove");
                        boolean isDelaying = false;
                        if (!isDelaying)
                            doFinish(0);
                    }
                } catch (Exception e) {
                    //reportError(e, "remove failed", getApplicationContext());
                    stopSelf();
                }
                if (packageName.equals("")) stopSelf();
                return START_NOT_STICKY;
            }
            displayWindow();
            PackageManager pm = getPackageManager();
            Resources appRes = null;
            final Bundle extras = intent.getExtras();

            handler.removeCallbacks(closeTimer);
            //handler.removeCallbacks(delayStop);

            String text = extras.getString("text");
            String title = extras.getString("title");
            packageName = extras.getString("packageName");
            key = extras.getString("key");
            tag = extras.getString("tag");
            id = extras.getInt("id", 0);
            final float sizeMultiplier = (float) (preferences.getInt("font_size", 100) / 100.0);

            Mlog.v(logTag, currentPackage);
            if ( preferences.getBoolean("block_current_app", true)
              && !isLocked
              && packageName.equals(currentPackage)
              && !packageName.equals("codes.simen.l50notifications")
            ) {
                Mlog.d(logTag, "Current package match - stopping");
                stopSelf();
                return START_NOT_STICKY;
            }
            Set<String> blockedApps = (Set<String>) ObjectSerializer.deserialize(preferences.getString("noshowlist", ""));
            if (blockedApps != null && blockedApps.size() > 0) {
                final boolean isBlacklistInverted = preferences.getBoolean("noshowlist_inverted", false);
                boolean contains = blockedApps.contains(currentPackage);
                Mlog.v(logTag, blockedApps.toString());
                Mlog.v(logTag+"NoShow", String.format("%s %s", String.valueOf(isBlacklistInverted), contains));
                if (
                      (!isBlacklistInverted && contains)
                    ||(isBlacklistInverted && !contains)
                ) {
                    Mlog.d(logTag+"NoShow", "Package match - stopping");
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }

            try {
                if (packageName.equals("codes.simen.voiceover"))
                    appRes = getResources();
                else
                    appRes = pm.getResourcesForApplication(packageName);
            } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                reportError(e, "", getApplicationContext());
            }

            View dismissButton = themeClass.getDismissButton(layout);
            if (preferences.getBoolean("hide_dismiss_button", true))
                themeClass.hideDismissButton(dismissButton);

            try {
                pendingIntent = (PendingIntent) extras.get("action");
            } catch (NullPointerException npe) {
                reportError(npe, "", getApplicationContext());
            }

            ImageView imageView = themeClass.getIconView(layout);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPopupClick(v, preferences.getBoolean("floating_window", false));
                }
            });

            try {
                if (Build.VERSION.SDK_INT >= 11) {
                    Drawable drawable = null;
                    Bitmap bitmap = (Bitmap) extras.get("iconLarge");
                    if (appRes != null && extras.containsKey("icon")) {
                        int icon_id = extras.getInt("icon");
                        drawable = appRes.getDrawable(icon_id);
                    } else {
                        try {
                            drawable = pm.getApplicationIcon(packageName);
                        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                            reportError(e, "", getApplicationContext());
                        }
                    }
                    ImageView smallIconView = themeClass.getSmallIconView(layout);
                    if (bitmap == null) {
                        if (drawable != null)
                            bitmap = drawableToBitmap(drawable);
                        if (smallIconView != null)
                            themeClass.setSmallIcon(smallIconView, null);
                    } else if (drawable != null && smallIconView != null)
                        themeClass.setSmallIcon(smallIconView, drawable);

                    if (bitmap != null) {
                        final int shortestSide;
                        final int width = bitmap.getWidth();
                        final int height = bitmap.getHeight();
                        if (width > height) shortestSide = height;
                        else                shortestSide = width;

                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, shortestSide, shortestSide, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        themeClass.setIcon( imageView, bitmap, preferences.getBoolean("round_icons", true) );
                    }
                }
            } catch (Exception e) {e.printStackTrace();}
            if (title.equals("")) {
                try {
                    title = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0));
                } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                    reportError(e, "", getApplicationContext());
                }
            }


            TextView titleTextView = (TextView) layout.findViewById(R.id.notification_title);
            TextView textView = (TextView) layout.findViewById(R.id.notification_subtitle);

            titleTextView.setText(title);

            final boolean privacy_on_lockscreen = isLocked && preferences.getBoolean("privacy_on_lockscreen", false);
            if (privacy_on_lockscreen)
                 textView.setText(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));
            else textView.setText(text);

            if (isCompact) textView.setMaxLines(MIN_LINES);
            else           textView.setMaxLines(MAX_LINES);

            final Resources resources = getResources();
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeMultiplier * resources.getDimension(R.dimen.text_size_notification_title));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeMultiplier * resources.getDimension(R.dimen.text_size_notification_text));


            if (Build.VERSION.SDK_INT >= 16 && !privacy_on_lockscreen) {
                try {
                    ViewGroup actionButtons = themeClass.getActionButtons(layout);
                    themeClass.removeActionButtons(actionButtons);
                    int i = extras.getInt("actionCount");
                    isActionButtons = i > 0;
                    if (isActionButtons) {
                        Mlog.d(logTag, String.valueOf(i));
                        themeClass.showActionButtons(layout, i);
                        while (i > 0) {
                            String actionTitle = extras.getString("action" + i + "title");
                            final PendingIntent actionIntent = (PendingIntent)
                                    extras.get("action" + i + "intent");

                            int actionIcon = extras.getInt("action" + i + "icon");
                            Drawable icon = null;
                            if (appRes != null) {
                                try {
                                    icon = appRes.getDrawable(actionIcon);
                                } catch (Resources.NotFoundException ignored) {}
                            }

                            themeClass.addActionButton(actionButtons, actionTitle, icon, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        if (themeClass.getRootView(layout).getTranslationX() != 0) return; // Stop if we're currently swiping. Bug 0000034
                                        openIntent(actionIntent, false);
                                        Mlog.d(logTag, "sendPendingAction");
                                    } catch (NullPointerException e) {
                                        reportError(e, "", getApplicationContext());
                                    }
                                }
                            }, sizeMultiplier);

                            i--;
                        }
                        if (isCompact)
                            themeClass.hideActionButtons(layout);
                    } else
                        themeClass.hideActionButtons(layout);
                } catch (NullPointerException npe) {// Ignored, usually happens in case of missing icons
                } catch (IndexOutOfBoundsException ignored) {
                } catch (RuntimeException rte) {
                    reportError(rte, "ThemeActionIcon", getApplicationContext());
                }
            } else {
                themeClass.hideActionButtons(layout);
            }

            dismissButton.setOnLongClickListener(blockTouchListener);

            if (Build.VERSION.SDK_INT >= 12) {
                ViewGroup self = themeClass.getRootView(layout);

                // Init swipe listener
                final SwipeDismissTouchListener dismissTouchListener = new SwipeDismissTouchListener(
                        self, position == 2 || position == 1, new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss() {
                        return true;
                    }

                    @Override
                    public boolean canExpand() {
                        return isCompact;
                    }

                    @Override
                    public void onDismiss(View view, Object token, int direction) {
                        //Mlog.v(logTag, "DIR" + direction);
                        switch (direction) {
                            case SwipeDismissTouchListener.DIRECTION_LEFT:
                            case SwipeDismissTouchListener.DIRECTION_RIGHT:
                                if (preferences.getBoolean("dismiss_on_swipe", true)) doFinish(1);
                                else                                                  doFinish(0);
                                break;
                            case SwipeDismissTouchListener.DIRECTION_UP:
                                doFinish(0);
                                break;
                            case SwipeDismissTouchListener.DIRECTION_DOWN:
                                expand();
                                return;
                            default:
                                Mlog.e(logTag, "Unknown direction: " + direction);
                                break;
                        }
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void outside() {
                        if (preferences.getBoolean("close_on_outside_touch", false))
                            doFinish(0);
                        if (isLocked && !isLocked())
                            doFinish(0);
                    }
                }
                );
                self.setClipChildren(false);
                self.setClipToPadding(false);
                final ArrayList<View> allChildren = getAllChildren(layout);
                if (allChildren.size() > 0)
                    for (View v : allChildren) {
                        v.setOnTouchListener(dismissTouchListener);
                    }

                // Animate in
                if (!prevPackageName.equals(packageName)) {
                    AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (displayTime < MAX_DISPLAY_TIME || !intent.getAction().equals("STAY")) {
                                handler.postDelayed(closeTimer, displayTime);
                                System.gc();
                            }
                        }
                    };
                    self.setTranslationX(0);
                    switch (position) {
                        case 2:
                        case 1:
                            self.setTranslationY(-300);
                            break;
                        case 0:
                            self.setTranslationX(-self.getWidth());
                            break;
                        case -1:
                            self.setTranslationY(300);
                    }
                    self.setAlpha(0.0f);
                    Float opacity = (float) preferences.getInt("opacity", 98);
                    if (opacity == null)
                        opacity = 98f;
                    try {
                        self.animate()
                                .setDuration(700)
                                .alpha(opacity / 100)
                                .translationY(0)
                                .translationX(0)
                                .setListener(listener);
                    } catch (NullPointerException npe) {
                        reportError(npe, "", getApplicationContext());
                        if (displayTime < MAX_DISPLAY_TIME || !intent.getAction().equals("STAY"))
                            handler.postDelayed(closeTimer, displayTime);
                    }
                    prevPackageName = packageName;
                } else {
                    if (displayTime < MAX_DISPLAY_TIME || !intent.getAction().equals("STAY")) {
                        handler.postDelayed(closeTimer, displayTime);
                    }
                }
            } else {
                textView.setMaxLines(MAX_LINES);
                if (displayTime < MAX_DISPLAY_TIME || !intent.getAction().equals("STAY")) {
                    handler.postDelayed(closeTimer, displayTime);
                }
            }

            layout.setVisibility(View.VISIBLE);

        } catch (Exception catchAllException) {
            reportError(catchAllException, "", getApplicationContext());
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private ArrayList<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    private final Handler handler = new Handler();
    private final Runnable closeTimer = new Runnable() {
        @Override
        public void run() {
            if (preferences.getBoolean("keep_on_lock_screen", false)) {
                if (isLocked()) {
                    if (!preferences.getBoolean("keep_screen_on_forever", false))
                        screenOff();
                    return;
                }

                Mlog.v(logTag, "not locked - removing notification");
            }
            final ViewGroup rootView = themeClass.getRootView(layout);
            if (rootView.getTranslationX() != 0 || rootView.getTranslationY() != 0) {
                handler.postDelayed(closeTimer, displayTime);
                return; // Stop if we're currently swiping.
            }
            if (displayTime == MAX_DISPLAY_TIME)
                return;

            doFinish(0);
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public void doStop(View v) {
        doFinish(1);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doHide(View v) {
        doFinish(0);
    }

    public void onPopupClick(View v) {
        onPopupClick(v, false);
    }

    public void onPopupClick(View v, boolean isFloating) {
        final ViewGroup rootView = themeClass.getRootView(layout);
        if (rootView.getTranslationX() != 0 || rootView.getTranslationY() != 0)
            return; // Stop if we're currently swiping. Bug 0000034

        if (!expand()) openIntent(pendingIntent, isFloating);
    }

    /*
     * Expand the heads-up. Returns true if the heads-up was expanded, false if it was expanded before calling this method.
     */
    private boolean expand() {
        if (!isCompact)
            return false;
        else {
            TextView subtitle = (TextView) layout.findViewById(R.id.notification_subtitle);
            if ( (subtitle.getLineCount() <= MIN_LINES && subtitle.length() < 80) && !isActionButtons) {
                return false;
            }
            isCompact = false;
            subtitle.setMaxLines(MAX_LINES);
            if (isActionButtons)
                themeClass.showActionButtons(layout, -1);
            if (displayTime < MAX_DISPLAY_TIME) {
                handler.removeCallbacks(closeTimer);
                handler.postDelayed(closeTimer, displayTime);
            }
            return true;
        }
    }

    void openIntent(PendingIntent mPendingIntent, boolean isFloating) {
        if (isLocked()) {
            startActivity(new Intent(getApplicationContext(), UnlockActivity.class)
                            .putExtra("action", mPendingIntent)
                            .putExtra("floating", isFloating)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
            doFinish(2);
        } else {
            try {
                Mlog.d(logTag, "sendPending");

                Intent intent = new Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (isFloating) intent.addFlags(FLAG_FLOATING_WINDOW);
                mPendingIntent.send(getApplicationContext(), 0, intent);
                doFinish(2);
            } catch (PendingIntent.CanceledException e) {
                //reportError(e, "App has canceled action", getApplicationContext());
                Toast.makeText(getApplicationContext(), getString(R.string.pendingintent_cancel_exception), Toast.LENGTH_SHORT).show();
                doFinish(0);
            } catch (NullPointerException e) {
                //reportError(e, "No action defined", getApplicationContext());
                Toast.makeText(getApplicationContext(), getString(R.string.pendingintent_null_exception), Toast.LENGTH_SHORT).show();
                doFinish(0);
            }
        }
    }

    private final View.OnLongClickListener blockTouchListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            try {
                Set<String> blacklist = (Set<String>) ObjectSerializer.deserialize(preferences.getString("blacklist", ""));
                if (blacklist == null) blacklist = new HashSet<String>();

                final boolean isBlacklistInverted = preferences.getBoolean("blacklist_inverted", false);
                Mlog.v(logTag, isBlacklistInverted);

                if (isBlacklistInverted) {
                    if (blacklist.contains(packageName) && blacklist.remove(packageName))
                        Toast.makeText(getApplicationContext(), getText(R.string.blocked_confirmation), Toast.LENGTH_SHORT).show();
                } else if (blacklist.add(packageName))
                    Toast.makeText(getApplicationContext(), getText(R.string.blocked_confirmation), Toast.LENGTH_SHORT).show();

                Mlog.v(logTag, blacklist);
                final String serialized = ObjectSerializer.serialize((Serializable) blacklist);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("blacklist", serialized);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            doFinish(1);
            return true;
        }
    };

    private void doFinish(final int doDismiss) { // 0=ikke fjern 1=fjern 2=åpnet
        Mlog.v(logTag + "DoFinish", doDismiss);
        handler.removeCallbacks(closeTimer);

        // Integrate with Voiceify
        if (doDismiss == 1 || doDismiss == 2) {
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent("codes.simen.notificationspeaker.STOP_READING");
            intent.putExtra("packageName", packageName);
            intent.putExtra("tag", tag);
            intent.putExtra("id", id);
            intent.setPackage("codes.simen.notificationspeaker");
            try {
                ResolveInfo resolveInfo= packageManager.resolveService(intent, 0);
                if (resolveInfo.serviceInfo != null) {
                    Mlog.d(logTag, "Voiceify found and resolved");
                    startService(intent);
                }
            } catch (NullPointerException ignored) {} // Don't panic! We'll survive without Voiceify
        }

        if (Build.VERSION.SDK_INT >= 12) {
            try {
                View self = layout.findViewById(R.id.notificationbg);
                ViewPropertyAnimator animator = self.animate()
                        .setDuration(300)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                layout.setVisibility(View.GONE);
                                if (doDismiss == 1)
                                    doDismiss(true);
                                else if (doDismiss == 2)
                                    doDismiss(false);
                                else {
                                    if (wLock != null && wLock.isHeld())
                                        wLock.release();
                                    stopSelf();
                                }
                            }
                        });
                if (doDismiss == 1) animator.translationX(-400);
                else if (doDismiss == 0)
                    switch (position) {
                        case 2:
                        case 1:
                            animator.translationY(-300);
                            break;
                        case -1:
                            animator.translationY(300);
                            break;
                    }
            } catch (Exception e) {
                reportError(e, "", getApplicationContext());
                e.printStackTrace();
                layout.setVisibility(View.GONE);
                if (doDismiss == 1)
                    doDismiss(true);
                else if (doDismiss == 2)
                    doDismiss(false);
                else {
                    if (wLock != null && wLock.isHeld())
                        wLock.release();
                    stopSelf();
                }
            }
        } else {
            layout.setVisibility(View.GONE);
            if (doDismiss == 1)
                doDismiss(true);
            else if (doDismiss == 2)
                doDismiss(false);
            else {
                if (wLock != null && wLock.isHeld())
                    wLock.release();
                stopSelf();
            }
        }
        prevPackageName = "0";
    }

    void doDismiss(boolean stopNow) {
        if (stopNow) {
            if (wLock != null && wLock.isHeld())
                wLock.release();
            stopSelf();
        } else {
            layout.setVisibility(View.GONE);
            /*Mlog.v(logTag, "delayStop");
            isDelaying = true;
            handler.postDelayed(delayStop, 10000);*/
            stopSelf();
        }

    }

    /*private final Runnable delayStop = new Runnable() {
        @Override
        public void run() {
            if (wLock != null && wLock.isHeld())
                wLock.release();
            stopSelf();
            isDelaying = false;
        }
    };*/

    @Override
    public void onDestroy () {
        super.onDestroy();
        Mlog.d(logTag, "Destroy");

        if (isViewAdded)
            windowManager.removeViewImmediate(layout);
        if (sensorManager != null)
            sensorManager.unregisterListener(this, sensor);
        if (wLock != null && wLock.isHeld())
            wLock.release();

        themeClass.destroy(layout);

        System.gc();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Mlog.v(logTag + "Sensor", String.valueOf(event.values[0]));
        isProximityClose = ( event.values[0] != sensor.getMaximumRange() );


        if (isProximityClose) screenOff();
        else                  screenOn();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Mlog.v(logTag + "SensorAccuracy", String.valueOf(accuracy));
    }

    void createWLock() {
        if (powerManager == null)
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        wLock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "heads-up");
    }

    void screenOn() {
        if (wLock == null) {
            createWLock();
        }
        if (!wLock.isHeld()) {
            Mlog.v(logTag, "wLock not held");
            if (preferences.getBoolean("keep_screen_on_forever", false)) {
                Mlog.v(logTag, "wLock forever");
                wLock.acquire();
            }
            else {
                Mlog.v(logTag, "wLock for " + displayTime);
                wLock.acquire(displayTime);
            }
        }
    }
    void screenOff() {
        if (wLock != null && wLock.isHeld()) wLock.release();

        if (powerManager == null)
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            if (policyManager == null)
                policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            if (policyManager.isAdminActive(AdminReceiver.getComponentName(getApplicationContext()))) {
                Mlog.v(logTag, "ADMIN_ACTIVE");
                policyManager.lockNow();
            } else Mlog.v(logTag, "ADMIN_NOT_ACTIVE");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private static void reportError(Exception e, String msg, Context c) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            if (e != null) {
                e.printStackTrace();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                msg = msg.concat(writer.toString());
                editor.putString("lastException", ObjectSerializer.serialize(e));
            }
            editor.putString("lastBug", msg);
            editor.apply();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

}
