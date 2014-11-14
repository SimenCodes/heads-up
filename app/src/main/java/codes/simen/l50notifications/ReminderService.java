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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class ReminderService extends Service {
    public static final int NOTIFICATION_ID = 20;
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_REMIND = "ACTION_REMIND";
    private static final String logTag = "ReminderService";


    public ReminderService() {
    }

    private final HashMap<Long, Bundle> reminders = new HashMap<Long, Bundle>();
    private final Handler handler = new Handler();
    private NotificationCompat.Builder builder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (ACTION_REMIND.equals(action)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final int reminderDelay = preferences.getInt("reminder_delay", 5000);

            final Bundle extras = intent.getExtras();

            reminders.put(System.currentTimeMillis() + reminderDelay, extras);

            handler.postDelayed(displayReminder, reminderDelay);

            builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_reminder)
                    .setContentTitle(String.format("%d reminders", reminders.size()))
                    .setContentText("Tap to cancel all reminders")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentIntent(PendingIntent.getService(
                            getApplicationContext(), 0,
                            new Intent(getApplicationContext(), ReminderService.class)
                                    .setAction(ACTION_STOP),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    ));

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } else if (ACTION_STOP.equals(action)) {
            stopSelf();
            Toast.makeText(getApplicationContext(), "All reminders cancelled", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    private final Runnable displayReminder = new Runnable() {
        @Override
        public void run() {
            if (reminders.size() > 0) {
                long currentTimeMillis = System.currentTimeMillis();
                try {
                    for (Long triggerTime : reminders.keySet()) {
                        if (triggerTime <= currentTimeMillis) {
                            final Bundle bundle = reminders.get(triggerTime);

                            Intent intent;
                            if (Build.VERSION.SDK_INT >= 18)
                                intent = new Intent(getApplicationContext(), OverlayService.class);
                            else
                                intent = new Intent(getApplicationContext(), OverlayServiceCommon.class);

                            intent.setAction(ACTION_REMIND);
                            intent.putExtras(bundle);
                            startService(intent);

                            reminders.remove(triggerTime);
                            break;
                        }
                    }
                } catch (ConcurrentModificationException cme) {
                    handler.postDelayed(displayReminder, 1000);
                }
            }
            if (reminders.size() == 0) stopSelf();
            else {
                builder.setContentTitle(String.format("%d reminders", reminders.size()));
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    };

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not allowed");
    }
}
