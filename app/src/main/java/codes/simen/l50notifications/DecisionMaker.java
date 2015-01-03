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

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import codes.simen.l50notifications.util.Mlog;
import codes.simen.l50notifications.util.ObjectSerializer;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
class DecisionMaker {
    public static final String ACTION_ADD = "codes.simen.l50notifications.action.ADD";
    public static final String ACTION_REMOVE = "codes.simen.l50notifications.action.REMOVE";

    public static final String EXTRA_NOTIFICATION = "codes.simen.l50notifications.extra.NOTIFICATION";
    public static final String EXTRA_PACKAGE_NAME = "codes.simen.l50notifications.extra.PACKAGE_NAME";
    public static final String EXTRA_TAG = "codes.simen.l50notifications.extra.TAG";
    public static final String EXTRA_ID = "codes.simen.l50notifications.extra.ID";
    public static final String EXTRA_SRC = "codes.simen.l50notifications.extra.SRC";

    public static final String logTag = "DecisionMaker";

    public void handleActionAdd(Notification notification, String packageName, String tag, int id, String key, Context context, String src) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Package filter
        Mlog.d(logTag, packageName);
        try {
            final Set<String> packageBlacklist = (Set<String>) ObjectSerializer.deserialize(preferences.getString("blacklist", ""));
            if (packageBlacklist != null) {
                final boolean isBlacklistInverted = preferences.getBoolean("blacklist_inverted", false);
                final boolean contains = packageBlacklist.contains(packageName);
                if      (!isBlacklistInverted && contains) return;
                else if (isBlacklistInverted && !contains) return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context.getApplicationContext(), "IOe " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ClassCastException e) {
            e.printStackTrace();
            Toast.makeText(context.getApplicationContext(), "CCe " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context.getApplicationContext(), "CNF " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Priority filter
        if (Build.VERSION.SDK_INT >= 16) {
            Set<String> priority_settings = preferences.getStringSet("notification_priority", null);
            if (priority_settings != null) {
                if (!priority_settings.contains(String.valueOf(notification.priority))) {
                    return;
                }
            }
        }

        String title = null;
        String text = null;


        // Get the text
        if (Build.VERSION.SDK_INT >= 21) {
            // Uncomment to test which extras a given notification contains
            /*for (String extraKey : notification.extras.keySet()) {
                Mlog.d(logTag, extraKey + "=" + notification.extras.get(extraKey));
            }*/
            title = notification.extras.getString("android.title");
            text = notification.extras.get("android.text").toString();

            String bigText = null;
            try {
                bigText = notification.extras.get("android.bigText").toString();
            } catch (Exception ignored) {}

            if (bigText != null && bigText.length() > 3) {
                text = bigText;
            }
        } else {
            // Old, hacky way. Close your eyes and skip this section.
            List<String> texts = null;
            try {
                texts = getText(notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (texts == null) {
                return;
            }
            if (texts.size() > 1) {
                Mlog.d(logTag, texts.toString());
                text = texts.get(1);
            }
            if (text == null)
                text = String.valueOf(notification.tickerText);
            if (texts.size() == 0)
                texts.add(text);
            if (text == null || text.equals("null"))
                return;

            title = texts.get(0);


            // Get the full content in older Android versions. Really ugly.
            if (Build.VERSION.SDK_INT >= 16) {
                if (notification.bigContentView != null) {
                    try {
                        Mlog.d(logTag, "bigView");
                        final String fullContent = fullContent(notification, context, texts, text);
                        if (fullContent != null) text = fullContent;
                    } catch (Resources.NotFoundException ignored) {
                    } catch (RuntimeException rte) {
                        try {
                            Looper.prepareMainLooper();
                        } catch (IllegalStateException ilse) {
                            try {
                                fullContent(notification, context, texts, text);
                                final String fullContent = fullContent(notification, context, texts, text);
                                if (fullContent != null) text = fullContent;
                                // Ignore all errors, we'll survive without the full notification
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }


        // Make an intent
        Intent intent = new Intent();
        intent.setAction(ACTION_ADD);

        if ("listener".equals(src)) intent.setClass(context, OverlayService.class);
        else                        intent.setClass(context, OverlayServiceCommon.class);

        Mlog.d(title, text);

        intent.putExtra("packageName", packageName);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("action", notification.contentIntent);

        if (Build.VERSION.SDK_INT >= 11)
            intent.putExtra("iconLarge", notification.largeIcon);
        intent.putExtra("icon", notification.icon);

        intent.putExtra("tag", tag);
        intent.putExtra("id", id);
        intent.putExtra("key", key);


        if (Build.VERSION.SDK_INT >= 19) {
            try {
                Notification.Action[] actions = notification.actions;
                if (actions != null) {
                    intent.putExtra("actionCount", actions.length);
                    Mlog.d(logTag, String.valueOf(actions.length));

                    int i = actions.length;
                    for (Notification.Action action : actions) {
                        if (i < 0) break; //No infinite loops, has happened once
                        Mlog.d(logTag, action.title);
                        intent.putExtra("action" + i + "icon", action.icon);
                        intent.putExtra("action" + i + "title", action.title);
                        intent.putExtra("action" + i + "intent", action.actionIntent);
                        i--;
                    }
                }
            } catch (IllegalAccessError iae) {
                Mlog.e(logTag, iae.getMessage());
            } catch (Exception e) {
                try {
                    String report = e.getMessage();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    report = report.concat(writer.toString());
                    if (preferences != null) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("lastBug", report);
                        editor.putString("lastException", ObjectSerializer.serialize(e));
                        editor.apply();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (preferences != null && preferences.getBoolean("broadcast_notifications", false)) {
            Mlog.d(logTag, "broadcast");
            final Intent broadcastIntent = new Intent(ACTION_ADD).putExtras(intent.getExtras());
            if (Build.VERSION.SDK_INT >= 16)
                broadcastIntent.putExtra("priority", notification.priority);
            context.sendBroadcast(broadcastIntent, "codes.simen.permission.NOTIFICATIONS");
        }

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK +
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS +
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
        );
        context.startService(intent);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private String fullContent(Notification notification, Context context, List<String> texts, String text) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup localView = (ViewGroup) inflater.inflate(notification.bigContentView.getLayoutId(), null);
        notification.bigContentView.reapply(context.getApplicationContext(), localView);

        ArrayList<View> allChildren = getAllChildren(localView);
        String viewTexts = "";
        for (View view : allChildren) {
            if (view instanceof TextView) {
                    Mlog.d(logTag, view.getClass().getSimpleName());
                    String mText = String.valueOf(((TextView) view).getText());
                    Mlog.d(logTag, mText);
                    if (!mText.equals(texts.get(0))
                            && mText.length() > 1
                            && !mText.matches("(([0]?[1-9]|1[0-2])([:.][0-5]\\d)(\\ [AaPp][Mm]))|(([0|1]?\\d?|2[0-3])([:.][0-5]\\d))")
                            && !view.getClass().getSimpleName().equals("Button")
                            ) {
                        //TODO: Check for texts identical to actions, as some apps doesn't use buttons for actions.
                        if (mText.startsWith(texts.get(0))) {
                            mText = mText.substring(texts.get(0).length());
                            if (mText.startsWith(":"))
                                mText = mText.substring(1);
                            if (mText.startsWith("\n"))
                                mText = mText.substring("\n".length());
                            if (mText.startsWith("\n"))
                                mText = mText.substring("\n".length());
                        }
                        Mlog.d(logTag, mText);
                        viewTexts = viewTexts.concat(mText).concat("\n");
                    }
            }
        }
        if (viewTexts.length() > 1 && viewTexts.length() > text.length()) {
            if (viewTexts.startsWith("\n"))
                viewTexts = viewTexts.substring("\n".length());
            Mlog.d(logTag, viewTexts);
            return viewTexts.substring(0, viewTexts.length() - 1);
        }
        return null;
    }

    public void handleActionRemove(String packageName, String tag, int id, Context applicationContext) {

        Intent intent = new Intent();
        intent.setClass(applicationContext, OverlayService.class);

        intent.setAction("REMOVE");
        intent.putExtra("tag", tag);
        intent.putExtra("id", id);
        intent.putExtra("packageName", packageName);

        applicationContext.startService(intent);
    }

    private static List<String> getText(Notification notification) {
        RemoteViews contentView = notification.contentView;
        /*if (Build.VERSION.SDK_INT >= 16) {
            contentView = notification.bigContentView;
        }*/

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<String>();
        try
        {
            Field field = contentView.getClass().getDeclaredField("mActions");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(contentView);

            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions)
            {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) continue;

                // View ID
                parcel.readInt();

                String methodName = parcel.readString();
                //noinspection ConstantConditions
                if (methodName == null) continue;

                    // Save strings
                else if (methodName.equals("setText"))
                {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();

                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    if (!text.contains(t)) {
                        text.add(t);
                    }
                }

                // Save times. Comment this section out if the notification time isn't important
                /*else if (methodName.equals("setTime"))
                {
                    // Parameter type (5 = Long)
                    parcel.readInt();

                    String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
                    text.add(t);
                }*/

                parcel.recycle();
            }
        }

        // It's not usually good style to do this, but then again, neither is the use of reflection...
        catch (Exception e)
        {
            Mlog.e("NotificationClassifier", e.toString());
            return null;
        }
        return text;
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
}